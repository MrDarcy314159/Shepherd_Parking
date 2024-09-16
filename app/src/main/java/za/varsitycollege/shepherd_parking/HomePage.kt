package za.varsitycollege.shepherd_parking

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
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
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import za.varsitycollege.shepherd_parking.UserManager

@Composable
fun HomePage(navController: NavController, userManager: UserManager) {
    // Variables to store the car count and max car count
    var carCount by remember { mutableStateOf(0) }
    var maxCarCount by remember { mutableStateOf(100) }
    var progress by remember { mutableStateOf(0f) }

    // Firebase Realtime Database reference
    val database = FirebaseDatabase.getInstance()
    val carCountRef = database.getReference("carCount")
    val maxCarCountRef = database.getReference("maxCarCount")

    // Fetch car count from Firebase
    carCountRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            carCount = snapshot.getValue(Int::class.java) ?: 0
            // Update the progress after ensuring maxCarCount has been fetched
            progress = if (maxCarCount > 0) carCount.toFloat() / maxCarCount.toFloat() else 0f
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Failed to read carCount: ${error.message}")
        }
    })

    // Fetch max car count from Firebase
    maxCarCountRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            maxCarCount = snapshot.getValue(Int::class.java) ?: 100
            // Update the progress after ensuring carCount has been fetched
            progress = if (maxCarCount > 0) carCount.toFloat() / maxCarCount.toFloat() else 0f
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Failed to read maxCarCount: ${error.message}")
        }
    })

    // Firebase Database reference
    val morningCarCountRef: DatabaseReference = database.getReference("projectedMorningCars")
    val afternoonCarCountRef: DatabaseReference = database.getReference("projectedAfternoonCars")

    // Remember the car count state
    val morningCarCount = remember { mutableStateOf(0) }
    val afternoonCarCount = remember { mutableStateOf(0) }

    // Time slots references
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

    // Remember the time slots state
    val timeSlots = remember { List(10) { mutableStateOf(0) } }

    // Load initial car count from Firebase
    LaunchedEffect(Unit) {
        // Load morning and afternoon car counts
        val morningSnapshot = morningCarCountRef.get().await()
        morningCarCount.value = morningSnapshot.getValue(Int::class.java) ?: 0

        val afternoonSnapshot = afternoonCarCountRef.get().await()
        afternoonCarCount.value = afternoonSnapshot.getValue(Int::class.java) ?: 0

        // Load time slots from Firebase
        timeSlotReferences.forEachIndexed { index, ref ->
            val snapshot = ref.get().await()
            timeSlots[index].value = snapshot.getValue(Int::class.java) ?: 0
        }
    }

    val context = LocalContext.current
    val email = userManager.getCurrentUserEmail()

    // Trigger for showing the student number dialog
    var showStudentNumberDialog by remember { mutableStateOf(false) }

    // Check if the student number is missing
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

    Log.d("Reference Date?", noFindDate.toString())

    // Check if any user has checked in today
    val todayDate = getTodaysDate()
    db.collection("check_in")
        .whereEqualTo("date", todayDate)
        .get()
        .addOnSuccessListener { result ->
            if (result.isEmpty){
                noFindDate = true
            } else{
                noFindDate = false
            }
        }
        .addOnFailureListener {
            noFindDate = false
        }

    Log.d("Reference Date?2", noFindDate.toString())

    // Reset car counts and time slots if no date is found
    if (noFindDate) {
        // Reset morning and afternoon car counts
        morningCarCount.value = 0
        morningCarCountRef.setValue(morningCarCount.value)

        afternoonCarCount.value = 0
        afternoonCarCountRef.setValue(afternoonCarCount.value)

        // Reset time slots
        timeSlots.forEachIndexed { index, slot ->
            slot.value = 0
            timeSlotReferences[index].setValue(slot.value)
        }
    }


    // Show dialog if student number is missing
    if (showStudentNumberDialog) {
        StudentNumberDialog(onSave = { studentNumber ->
            email?.let { userManager.saveStudentNumber(it, studentNumber) }
            showStudentNumberDialog = false
        })
    }

    // Box layout for the home page content
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
            // Header Bubble
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
                                text = "SHEPHERD PARKING",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.DarkGray
                            )
                            Text(
                                text = "Home Page",
                                fontSize = 18.sp,
                                color = AppColors.DarkGray
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.sheep),
                            contentDescription = "Sheep Logo",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(AppColors.MintGreen)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Parking Capacity Card
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
                        text = "Parking Availability : ${(progress * 100).toInt()}% FULL",
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

            // Main Content Card with two rows of buttons
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // First Row of Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SquareIconButton(
                            drawableId = R.drawable.check_in,
                            contentDescription = "Check In",
                            onClick = { navController.navigate("check_in") },
                            text = "Check In",
                            explanation = "Record your arrival at the parking area"
                        )

                        SquareIconButton(
                            drawableId = R.drawable.traffic_feedback_image,
                            contentDescription = "Traffic Feedback",
                            onClick = { navController.navigate("traffic_feedback") },
                            text = "Traffic Feedback",
                            explanation = "Report current traffic conditions"
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    // Second Row of Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SquareIconButton(
                            drawableId = R.drawable.late_image,
                            contentDescription = "Late Notification",
                            onClick = { navController.navigate("late") },
                            text = "Late",
                            explanation = "Notify if you're running late"
                        )
                        SquareIconButton(
                            drawableId = R.drawable.analytics_image,
                            contentDescription = "Analytics",
                            onClick = { navController.navigate("analytics") },
                            text = "Analytics",
                            explanation = "View parking usage statistics"
                        )
                    }
                }
            }
        }



        // Varsity College logo at the bottom
        Image(
            painter = painterResource(id = R.drawable.varsity_college_icon),
            contentDescription = "Varsity College Logo",
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
                text = "Enter Student Number",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Please enter your student number to complete your registration.",
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = studentNumber,
                    onValueChange = { studentNumber = it },
                    label = { Text("Student Number", color = Color.Gray) },
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
                Text("Save")
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

// Helper function to get the current date in DD/MM/YYYY format
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
