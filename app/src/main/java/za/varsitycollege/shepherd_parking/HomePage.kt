package za.varsitycollege.shepherd_parking

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import za.varsitycollege.shepherd_parking.UserManager

@Composable
fun HomePage(navController: NavController, userManager: UserManager) {
    var carCount by remember { mutableStateOf(0) }
    var maxCarCount by remember { mutableStateOf(100) }
    var progress by remember { mutableStateOf(0f) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // Show toast message
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            toastMessage = null
        }
    }

    // Firebase Realtime Database reference
    val database = FirebaseDatabase.getInstance()
    val carCountRef = database.getReference("carCount")
    val maxCarCountRef = database.getReference("maxCarCount")

    // Fetch car count from Firebase
    carCountRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            carCount = snapshot.getValue(Int::class.java) ?: 0
            progress = if (maxCarCount > 0) carCount.toFloat() / maxCarCount.toFloat() else 0f
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Failed to read carCount: ${error.message}")
            toastMessage = context.getString(R.string.failed_to_read_car_count)
        }
    })

    maxCarCountRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            maxCarCount = snapshot.getValue(Int::class.java) ?: 100
            progress = if (maxCarCount > 0) carCount.toFloat() / maxCarCount.toFloat() else 0f
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Failed to read maxCarCount: ${error.message}")
            toastMessage = context.getString(R.string.failed_to_read_max_car_count)
        }
    })

    val morningCarCountRef: DatabaseReference = database.getReference("projectedMorningCars")
    val afternoonCarCountRef: DatabaseReference = database.getReference("projectedAfternoonCars")

    val morningCarCount = remember { mutableStateOf(0) }
    val afternoonCarCount = remember { mutableStateOf(0) }

    val timeSlotReferences = listOf(
        database.getReference("timeSlots/sevenToEight"),
        database.getReference("timeSlots/eightToNine"),
        database.getReference("timeSlots/nineToTen"),
        database.getReference("timeSlots/tenToEleven"),
        database.getReference("timeSlots/elevenToTwelve"),
        database.getReference("timeSlots/twelveToOne"),
        database.getReference("timeSlots/oneToTwo"),
        database.getReference("timeSlots/twoToThree"),
        database.getReference("timeSlots/threeToFour"),
        database.getReference("timeSlots/fourToFive")
    )

    val timeSlots = remember { List(10) { mutableStateOf(0) } }

    LaunchedEffect(Unit) {
        try {
            val morningSnapshot = morningCarCountRef.get().await()
            morningCarCount.value = morningSnapshot.getValue(Int::class.java) ?: 0

            val afternoonSnapshot = afternoonCarCountRef.get().await()
            afternoonCarCount.value = afternoonSnapshot.getValue(Int::class.java) ?: 0

            timeSlotReferences.forEachIndexed { index, ref ->
                val snapshot = ref.get().await()
                timeSlots[index].value = snapshot.getValue(Int::class.java) ?: 0
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Failed to load initial data: ${e.message}")
            toastMessage = context.getString(R.string.failed_to_load_initial_data)
        }
    }

    val email = userManager.getCurrentUserEmail()

    var showStudentNumberDialog by remember { mutableStateOf(false) }

    LaunchedEffect(email) {
        email?.let {
            userManager.isStudentNumberMissing(it) { isMissing ->
                if (isMissing) {
                    showStudentNumberDialog = true
                }
            }
        }
    }

    val db = remember { FirebaseFirestore.getInstance() }

    var noFindDate by remember { mutableStateOf(false) }

    val todayDate = getTodaysDate()
    db.collection("check_in")
        .whereEqualTo("date", todayDate)
        .get()
        .addOnSuccessListener { result ->
            noFindDate = result.isEmpty
        }
        .addOnFailureListener {
            noFindDate = false
            toastMessage = context.getString(R.string.failed_to_check_date)
        }

    if (noFindDate) {
        morningCarCount.value = 0
        morningCarCountRef.setValue(morningCarCount.value)

        afternoonCarCount.value = 0
        afternoonCarCountRef.setValue(afternoonCarCount.value)

        timeSlots.forEachIndexed { index, slot ->
            slot.value = 0
            timeSlotReferences[index].setValue(slot.value)
        }
    }

    if (showStudentNumberDialog) {
        StudentNumberDialog(onSave = { studentNumber ->
            email?.let { userManager.saveStudentNumber(it, studentNumber) }
            showStudentNumberDialog = false
        })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.MintGreen)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.app_name),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.DarkGray
                            )
                            Text(
                                text = stringResource(R.string.home_page),
                                fontSize = 18.sp,
                                color = AppColors.DarkGray
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.sheep),
                            contentDescription = stringResource(R.string.sheep_logo_description),
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(AppColors.MintGreen)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.parking_availability, (progress * 100).toInt()),
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = AppColors.MintGreen,
                        trackColor = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SquareIconButton(
                            drawableId = R.drawable.check_in,
                            contentDescription = stringResource(R.string.check_in),
                            onClick = { navController.navigate("check_in") },
                            text = stringResource(R.string.check_in),
                            explanation = stringResource(R.string.check_in_explanation)
                        )

                        SquareIconButton(
                            drawableId = R.drawable.traffic_feedback_image,
                            contentDescription = stringResource(R.string.traffic_feedback),
                            onClick = { navController.navigate("traffic_feedback") },
                            text = stringResource(R.string.traffic_feedback),
                            explanation = stringResource(R.string.traffic_feedback_explanation)
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SquareIconButton(
                            drawableId = R.drawable.late_image,
                            contentDescription = stringResource(R.string.late_notification),
                            onClick = { navController.navigate("late") },
                            text = stringResource(R.string.late),
                            explanation = stringResource(R.string.late_explanation)
                        )
                        SquareIconButton(
                            drawableId = R.drawable.analytics_image,
                            contentDescription = stringResource(R.string.analytics),
                            onClick = { navController.navigate("analytics") },
                            text = stringResource(R.string.analytics),
                            explanation = stringResource(R.string.analytics_explanation)
                        )
                    }
                }
            }
        }

        Image(
            painter = painterResource(id = R.drawable.varsity_college_icon),
            contentDescription = stringResource(R.string.varsity_college_logo_description),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .height(50.dp)
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.varsitycollege.co.za/"))
                    context.startActivity(intent)
                }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentNumberDialog(
    onSave: (String) -> Unit
) {
    var studentNumber by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { /* Disable dismiss */ },
        containerColor = Color.White,
        title = {
            Text(
                text = stringResource(R.string.enter_student_number),
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.enter_student_number_explanation),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = studentNumber,
                    onValueChange = { studentNumber = it },
                    label = { Text(stringResource(R.string.student_number), color = Color.Gray) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Gray,
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor = Color.Black,
                        focusedLabelColor = Color.Gray,
                        unfocusedLabelColor = Color.Gray
                    ),
                    textStyle = TextStyle(color = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (studentNumber.isNotBlank()) {
                        onSave(studentNumber)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA8E2D0),
                    contentColor = Color.Black
                )
            ) {
                Text(stringResource(R.string.save))
            }
        }
    )
}

@Composable
fun SquareIconButton(
    drawableId: Int,
    contentDescription: String,
    onClick: () -> Unit,
    text: String,
    explanation: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(150.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(2.dp, AppColors.MintGreen, RoundedCornerShape(8.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = contentDescription,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = text,
                    color = AppColors.DarkGray,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = explanation,
            fontSize = 12.sp,
            color = AppColors.DarkGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

fun getTodaysDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date())
}

@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
    val navController = rememberNavController()
    MaterialTheme {
        HomePage(navController, userManager = UserManager(LocalContext.current))
    }
}
