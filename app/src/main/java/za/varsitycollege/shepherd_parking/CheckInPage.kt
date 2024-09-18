package za.varsitycollege.shepherd_parking

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInPage(navController: NavController) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val user = auth.currentUser
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var emailValue by remember { mutableStateOf("") }
    var studentNumber by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var checkInTime by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var removeSuccess by remember { mutableStateOf(false) }
    var showDropdown by remember { mutableStateOf(false) }
    var checkInAllowed by remember { mutableStateOf(true) }
    var showAlreadyCheckedInDialog by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val morningString = stringResource(R.string.morning)
    val afternoonString = stringResource(R.string.afternoon)
    val options = listOf(morningString, afternoonString)

    // Show toast message
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            toastMessage = null
        }
    }

    val morningTimeSlots = listOf("7:00-8:00", "8:00-9:00", "9:00-10:00", "10:00-11:00", "11:00-12:00")
    val afternoonTimeSlots = listOf("12:00-13:00", "13:00-14:00", "14:00-15:00", "15:00-16:00", "16:00-17:00")

    var timeSlot by remember { mutableStateOf("") }
    var showTimeSlotDropdown by remember { mutableStateOf(false) }

    val userManager = remember { UserManager(context) }

    LaunchedEffect(user) {
        user?.email?.let { email ->
            userManager.fetchUserDetails(email) { fetchedUser ->
                fetchedUser?.let {
                    name = it.name
                    surname = it.surname
                    studentNumber = it.studentNumber
                    emailValue = it.email
                }
            }

            val todayDate = getTodayDate()
            db.collection("check_in")
                .whereEqualTo("stdNumber", studentNumber)
                .whereEqualTo("date", todayDate)
                .get()
                .addOnSuccessListener { result ->
                    checkInAllowed = result.isEmpty
                }
                .addOnFailureListener {
                    checkInAllowed = false
                }
        }
    }

    val todayDate = getTodayDate()
    Log.d("Time Function", "Today's Date Function: $todayDate")

    db.collection("check_in")
        .whereEqualTo("stdNumber", studentNumber)
        .whereEqualTo("date", todayDate)
        .get()
        .addOnSuccessListener { result ->
            if (!result.isEmpty) {
                for (query in result.documents) {
                    checkInTime = query.getString("time") ?: ""
                    Log.d("Check_In", "Check-in time: $checkInTime")
                }
                checkInAllowed = false
            } else {
                checkInAllowed = true
            }
            Log.d("Check_In 2", "Check-in allowed: $checkInAllowed")
        }
        .addOnFailureListener { exception ->
            checkInAllowed = false
            Log.e("Check_In 3", "Error checking in status", exception)
        }

    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val morningCarCountRef: DatabaseReference = database.getReference("projectedMorningCars")
    val afternoonCarCountRef: DatabaseReference = database.getReference("projectedAfternoonCars")

    val timeSlotsRef1: DatabaseReference = database.getReference("timeSlots/sevenToEight")
    val timeSlotsRef2: DatabaseReference = database.getReference("timeSlots/eightToNine")
    val timeSlotsRef3: DatabaseReference = database.getReference("timeSlots/nineToTen")
    val timeSlotsRef4: DatabaseReference = database.getReference("timeSlots/tenToEleven")
    val timeSlotsRef5: DatabaseReference = database.getReference("timeSlots/elevenToTwelve")
    val timeSlotsRef6: DatabaseReference = database.getReference("timeSlots/twelveToOne")
    val timeSlotsRef7: DatabaseReference = database.getReference("timeSlots/oneToTwo")
    val timeSlotsRef8: DatabaseReference = database.getReference("timeSlots/twoToThree")
    val timeSlotsRef9: DatabaseReference = database.getReference("timeSlots/threeToFour")
    val timeSlotsRef10: DatabaseReference = database.getReference("timeSlots/fourToFive")

    val morningCarCount = remember { mutableStateOf(0) }
    val afternoonCarCount = remember { mutableStateOf(0) }

    val timeSlots1 = remember { mutableStateOf(0) }
    val timeSlots2 = remember { mutableStateOf(0) }
    val timeSlots3 = remember { mutableStateOf(0) }
    val timeSlots4 = remember { mutableStateOf(0) }
    val timeSlots5 = remember { mutableStateOf(0) }
    val timeSlots6 = remember { mutableStateOf(0) }
    val timeSlots7 = remember { mutableStateOf(0) }
    val timeSlots8 = remember { mutableStateOf(0) }
    val timeSlots9 = remember { mutableStateOf(0) }
    val timeSlots10 = remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val morningSnapshot = morningCarCountRef.get().await()
        morningCarCount.value = morningSnapshot.getValue(Int::class.java) ?: 0
        val afternoonSnapshot = afternoonCarCountRef.get().await()
        afternoonCarCount.value = afternoonSnapshot.getValue(Int::class.java) ?: 0

        val timeSlot1Snapshot = timeSlotsRef1.get().await()
        timeSlots1.value = timeSlot1Snapshot.getValue(Int::class.java) ?: 0

        val timeSlot2Snapshot = timeSlotsRef2.get().await()
        timeSlots2.value = timeSlot2Snapshot.getValue(Int::class.java) ?: 0

        val timeSlot3Snapshot = timeSlotsRef3.get().await()
        timeSlots3.value = timeSlot3Snapshot.getValue(Int::class.java) ?: 0

        val timeSlot4Snapshot = timeSlotsRef4.get().await()
        timeSlots4.value = timeSlot4Snapshot.getValue(Int::class.java) ?: 0

        val timeSlot5Snapshot = timeSlotsRef5.get().await()
        timeSlots5.value = timeSlot5Snapshot.getValue(Int::class.java) ?: 0

        val timeSlot6Snapshot = timeSlotsRef6.get().await()
        timeSlots6.value = timeSlot6Snapshot.getValue(Int::class.java) ?: 0

        val timeSlot7Snapshot = timeSlotsRef7.get().await()
        timeSlots7.value = timeSlot7Snapshot.getValue(Int::class.java) ?: 0

        val timeSlot8Snapshot = timeSlotsRef8.get().await()
        timeSlots8.value = timeSlot8Snapshot.getValue(Int::class.java) ?: 0

        val timeSlot9Snapshot = timeSlotsRef9.get().await()
        timeSlots9.value = timeSlot9Snapshot.getValue(Int::class.java) ?: 0

        val timeSlot10Snapshot = timeSlotsRef10.get().await()
        timeSlots10.value = timeSlot10Snapshot.getValue(Int::class.java) ?: 0
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
            // Top Bar Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
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
                                text = stringResource(R.string.check_in),
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

            // Main Content Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Displaying user info
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.MintGreen),
                        border = BorderStroke(2.dp, AppColors.DarkGray),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$name $surname",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = studentNumber,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }

                    // First dropdown for selecting Morning or Afternoon
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showDropdown = !showDropdown },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = AppColors.DarkGray),
                            border = BorderStroke(2.dp, AppColors.DarkGray)
                        ) {
                            Text(if (time.isEmpty()) stringResource(R.string.select_time_morning_afternoon) else time)
                        }
                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            options.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        time = option
                                        showDropdown = false
                                        timeSlot = ""
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Conditionally display the time slots based on Morning or Afternoon selection
                    if (time == morningString || time == afternoonString) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { showTimeSlotDropdown = !showTimeSlotDropdown },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = AppColors.DarkGray),
                                border = BorderStroke(2.dp, AppColors.DarkGray)
                            ) {
                                Text(if (timeSlot.isEmpty()) stringResource(R.string.select_specific_time_slot) else timeSlot)
                            }
                            DropdownMenu(
                                expanded = showTimeSlotDropdown,
                                onDismissRequest = { showTimeSlotDropdown = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val timeSlots = if (time == morningString) morningTimeSlots else afternoonTimeSlots
                                timeSlots.forEach { slot ->
                                    DropdownMenuItem(
                                        text = { Text(slot) },
                                        onClick = {
                                            timeSlot = slot
                                            showTimeSlotDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (time.isNotBlank() && timeSlot.isNotBlank()) {
                                if (checkInAllowed) {
                                    var timeSlotString = ""

                                    // Proceed with check-in logic and storing data in Firestore
                                    if (time == morningString) {
                                        morningCarCount.value += 1
                                        morningCarCountRef.setValue(morningCarCount.value)
                                        // Increment the correct time slot based on user selection
                                        when (timeSlot) {
                                            "7:00-8:00" -> {
                                                timeSlots1.value += 1
                                                timeSlotsRef1.setValue(timeSlots1.value)
                                                timeSlotString = "sevenToEight"
                                            }
                                            "8:00-9:00" -> {
                                                timeSlots2.value += 1
                                                timeSlotsRef2.setValue(timeSlots2.value)
                                                timeSlotString = "eightToNine"
                                            }
                                            "9:00-10:00" -> {
                                                timeSlots3.value += 1
                                                timeSlotsRef3.setValue(timeSlots3.value)
                                                timeSlotString = "nineToTen"
                                            }
                                            "10:00-11:00" -> {
                                                timeSlots4.value += 1
                                                timeSlotsRef4.setValue(timeSlots4.value)
                                                timeSlotString = "tenToEleven"
                                            }
                                            "11:00-12:00" -> {
                                                timeSlots5.value += 1
                                                timeSlotsRef5.setValue(timeSlots5.value)
                                                timeSlotString = "elevenToTwelve"
                                            }
                                        }
                                    } else if (time == afternoonString) {
                                        afternoonCarCount.value += 1
                                        afternoonCarCountRef.setValue(afternoonCarCount.value)

                                        when (timeSlot) {
                                            "12:00-13:00" -> {
                                                timeSlots6.value += 1
                                                timeSlotsRef6.setValue(timeSlots6.value)
                                                timeSlotString = "twelveToOne"
                                            }
                                            "13:00-14:00" -> {
                                                timeSlots7.value += 1
                                                timeSlotsRef7.setValue(timeSlots7.value)
                                                timeSlotString = "oneToTwo"
                                            }
                                            "14:00-15:00" -> {
                                                timeSlots8.value += 1
                                                timeSlotsRef8.setValue(timeSlots8.value)
                                                timeSlotString = "twoToThree"
                                            }
                                            "15:00-16:00" -> {
                                                timeSlots9.value += 1
                                                timeSlotsRef9.setValue(timeSlots9.value)
                                                timeSlotString = "threeToFour"
                                            }
                                            "16:00-17:00" -> {
                                                timeSlots10.value += 1
                                                timeSlotsRef10.setValue(timeSlots10.value)
                                                timeSlotString = "fourToFive"
                                            }
                                        }
                                    }

                                    // Store check-in data in Firestore
                                    val checkInData = hashMapOf(
                                        "name" to name,
                                        "surname" to surname,
                                        "stdNumber" to studentNumber,
                                        "time" to time,
                                        "timeSlot" to timeSlotString,
                                        "date" to getTodayDate()
                                    )

                                    db.collection("check_in")
                                        .add(checkInData)
                                        .addOnSuccessListener {
                                            Log.d("Firestore", "Check-in data successfully stored!")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("Firestore", "Error storing check-in data", e)
                                        }

                                    showSuccessDialog = true

                                    val handler = Handler(Looper.getMainLooper())
                                    handler.postDelayed({
                                        showSuccessDialog = false
                                        navController.navigate("check_in") {
                                            popUpTo(navController.graph.startDestinationId) {
                                                inclusive = true
                                            }
                                        }
                                    }, 2000)
                                } else {
                                    showAlreadyCheckedInDialog = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = AppColors.DarkGray
                        ),
                        border = BorderStroke(2.dp, AppColors.DarkGray),
                        enabled = time.isNotBlank() && timeSlot.isNotBlank()
                    ) {
                        Text(stringResource(R.string.submit))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (!checkInAllowed) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = stringResource(R.string.checked_in),
                                tint = Color.Green,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.check_in_status),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = stringResource(R.string.checked_in_for_tomorrow, checkInTime.lowercase()),
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = stringResource(R.string.not_checked_in),
                                tint = Color.Red,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.check_in_status),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = stringResource(R.string.not_checked_in_yet),
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            if (!checkInAllowed) {
                                removeSuccess = true
                                removeCheckIn(studentNumber, emailValue, context)
                                navController.navigate("check_in")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.DarkGray),
                        enabled = !checkInAllowed
                    ) {
                        Text(stringResource(R.string.remove_check_in))
                    }

                    if (showSuccessDialog) {
                        Dialog(onDismissRequest = { showSuccessDialog = false }) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.check_in_success_message),
                                        color = AppColors.DarkGray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    if (showAlreadyCheckedInDialog) {
                        Dialog(onDismissRequest = { showAlreadyCheckedInDialog = false }) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.already_checked_in_today),
                                        color = AppColors.DarkGray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun removeCheckIn(studentNumber: String, emailValue: String, context: Context) {
    val db = FirebaseFirestore.getInstance()
    val database = FirebaseDatabase.getInstance()

    val morningCarCountRef: DatabaseReference = database.getReference("projectedMorningCars")
    val afternoonCarCountRef: DatabaseReference = database.getReference("projectedAfternoonCars")

    val date = getTodayDate()

    db.collection("check_in")
        .whereEqualTo("stdNumber", studentNumber)
        .whereEqualTo("date", date)
        .get()
        .addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                for (document in querySnapshot.documents) {
                    val time = document.getString("time")
                    val timeSlot = document.getString("timeSlot")

                    db.collection("check_in").document(document.id).delete()
                        .addOnSuccessListener {
                            Log.d("Check_In", "Check-in removed for user: $studentNumber")

                            when (time) {
                                "Morning" -> {
                                    morningCarCountRef.get().addOnSuccessListener { snapshot ->
                                        val currentCount = snapshot.getValue(Int::class.java) ?: 0
                                        morningCarCountRef.setValue(currentCount - 1)
                                    }
                                }
                                "Afternoon" -> {
                                    afternoonCarCountRef.get().addOnSuccessListener { snapshot ->
                                        val currentCount = snapshot.getValue(Int::class.java) ?: 0
                                        afternoonCarCountRef.setValue(currentCount - 1)
                                    }
                                }
                                else -> {
                                    Log.e("Check_In", "Unknown time value: $time")
                                }
                            }

                            if (timeSlot != null) {
                                val timeSlotRef: DatabaseReference = database.getReference("timeSlots").child(timeSlot)
                                timeSlotRef.get().addOnSuccessListener { snapshot ->
                                    val currentCount = snapshot.getValue(Int::class.java) ?: 0
                                    if (currentCount > 0) {
                                        timeSlotRef.setValue(currentCount - 1)
                                    }
                                }
                            }

                            Toast.makeText(context, context.getString(R.string.check_in_removed_successfully), Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Check_In", "Error removing check-in", e)
                        }
                }
            } else {
                Log.d("Check_In", "No check-in found for user: $studentNumber")
                Toast.makeText(context, context.getString(R.string.no_check_in_record_found), Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener { e ->
            Log.e("Check_In", "Error fetching check-in", e)
        }
}

fun getTodayDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date())
}