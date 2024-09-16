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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log

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
    val options = listOf("Morning", "Afternoon")
    var showAlreadyCheckedInDialog by remember { mutableStateOf(false) }

    // Time slot options for morning and afternoon
    val morningTimeSlots = listOf("7:00-8:00", "8:00-9:00", "9:00-10:00", "10:00-11:00", "11:00-12:00")
    val afternoonTimeSlots = listOf("12:00-13:00", "13:00-14:00", "14:00-15:00", "15:00-16:00", "16:00-17:00")

    // State to manage time slot dropdown visibility and selection
    var timeSlot by remember { mutableStateOf("") }
    var showTimeSlotDropdown by remember { mutableStateOf(false) }

    // Initialize the UserManager with context
    val userManager = remember { UserManager(context) }

    // Load user data from UserManager
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

            // Check if user has checked in today
            val todayDate = getTodayDate()
            db.collection("check_in")
                .whereEqualTo("stdNumber", studentNumber)
                .whereEqualTo("date", todayDate)
                .get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty){
                        checkInAllowed = true
                    } else{
                        checkInAllowed = false
                    }
                }
                .addOnFailureListener {
                    checkInAllowed = false
                }
        }
    }

    //testing to the logcat
    Log.d("Check_In 1","Check In Status : $checkInAllowed")

    // Check if user has checked in today
    val todayDate = getTodayDate()
    Log.d("Time Function", "Today's Date Function: $todayDate")

    //get the check in time from the database as well as check-in allowed ?
    db.collection("check_in")
        .whereEqualTo("stdNumber", studentNumber)
        .whereEqualTo("date", todayDate)
        .get()
        .addOnSuccessListener { result ->
            // Check if any documents are returned
            if (!result.isEmpty) {
                for (query in result.documents) {
                    checkInTime = query.getString("time") ?: ""
                    Log.d("Check_In", "Check-in time: $checkInTime")
                }
                checkInAllowed = false  // If a document is found, check-in is not allowed again for the day
            } else {
                checkInAllowed = true  // If no documents are found, check-in is allowed
            }
            Log.d("Check_In 2", "Check-in allowed: $checkInAllowed")
        }
        .addOnFailureListener { exception ->
            checkInAllowed = false
            Log.e("Check_In 3", "Error checking in status", exception)
        }

    // Firebase Database reference
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val morningCarCountRef: DatabaseReference = database.getReference("projectedMorningCars")
    val afternoonCarCountRef: DatabaseReference = database.getReference("projectedAfternoonCars")

    //time slots
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

    // Remember the car count state
    val morningCarCount = remember { mutableStateOf(0) }
    val afternoonCarCount = remember { mutableStateOf(0) }

    //timeslots
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

    // Load initial car count from Firebase
    LaunchedEffect(Unit) {
        val morningSnapshot = morningCarCountRef.get().await()
        morningCarCount.value = morningSnapshot.getValue(Int::class.java) ?: 0
        val afternoonSnapshot = afternoonCarCountRef.get().await()
        afternoonCarCount.value = afternoonSnapshot.getValue(Int::class.java) ?: 0

        //time slots
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
                                text = "SHEPHERD PARKING",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.DarkGray
                            )
                            Text(
                                text = "Check In",
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
                                text = "$studentNumber",
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
                            Text(if (time.isEmpty()) "Select Time (Morning/Afternoon)" else time)
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
                                        // Clear time slot when new time (Morning/Afternoon) is selected
                                        timeSlot = ""
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Conditionally display the time slots based on Morning or Afternoon selection
                    if (time == "Morning" || time == "Afternoon") {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { showTimeSlotDropdown = !showTimeSlotDropdown },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = AppColors.DarkGray),
                                border = BorderStroke(2.dp, AppColors.DarkGray)
                            ) {
                                Text(if (timeSlot.isEmpty()) "Select Specific Time Slot" else timeSlot)
                            }
                            DropdownMenu(
                                expanded = showTimeSlotDropdown,
                                onDismissRequest = { showTimeSlotDropdown = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val timeSlots = if (time == "Morning") morningTimeSlots else afternoonTimeSlots
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
                                    if (time == "Morning") {
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

                                    } else if (time == "Afternoon"){
                                        afternoonCarCount.value += 1
                                        afternoonCarCountRef.setValue(afternoonCarCount.value)

                                        // Increment the correct time slot based on user selection
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

                                    // Show success dialog
                                    showSuccessDialog = true

                                    // Dismiss the dialog and navigate back after a delay
                                    val handler = Handler(Looper.getMainLooper())
                                    handler.postDelayed({
                                        showSuccessDialog = false
                                        navController.navigate("check_in") {
                                            popUpTo(navController.graph.startDestinationId) {
                                                inclusive = true
                                            }
                                        }
                                    }, 2000) // Delay for 2 seconds
                                } else {
                                    // User has already checked in today
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
                        Text("Submit")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Log.d("Check- In Status", "$checkInTime")

                    //shows the users check in status
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
                                contentDescription = "Checked In",
                                tint = Color.Green,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Check-In Status",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "You are checked in for tomorrow ${checkInTime.lowercase()}.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Not Checked In",
                                tint = Color.Red,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Check-In Status",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "You are not checked in yet.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Add Remove check in Button
                    OutlinedButton(
                        onClick = {
                            if (!checkInAllowed) {
                                removeSuccess = true
                                removeCheckIn(studentNumber, emailValue, context) // Pass the context here
                                navController.navigate("check_in")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),  // Space between button and dropdown
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.DarkGray),
                        enabled = !checkInAllowed // Disable the button when checkInAllowed is true
                    ) {
                        Text("Remove Check In")
                    }

                    //dialog for when user checks in
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
                                        text = "Thank you for checking in to campus parking. See you tomorrow!",
                                        color = AppColors.DarkGray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    // Display the already checked-in dialog if showAlreadyCheckedInDialog is true
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
                                        text = "You have already checked into campus today. Please try again tomorrow!",
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

//function to remove the users check in status
fun removeCheckIn(studentNumber: String, emailValue: String, context: Context) {
    val db = FirebaseFirestore.getInstance()
    val database = FirebaseDatabase.getInstance()

    // References to Firebase Realtime Database nodes
    val morningCarCountRef: DatabaseReference = database.getReference("projectedMorningCars")
    val afternoonCarCountRef: DatabaseReference = database.getReference("projectedAfternoonCars")

    // Get current date in dd/MM/yyyy format
    val date = getTodayDate()

    // Query to find the matching check-in for the user for today
    db.collection("check_in")
        .whereEqualTo("stdNumber", studentNumber)
        .whereEqualTo("date", date)
        .get()
        .addOnSuccessListener { querySnapshot ->
            // If the user has a check-in for today, remove it
            if (!querySnapshot.isEmpty) {
                for (document in querySnapshot.documents) {
                    // Retrieve the 'time' and 'timeSlot' fields from the Firestore document
                    val time = document.getString("time")
                    val timeSlot = document.getString("timeSlot")

                    // Remove the document from Firestore
                    db.collection("check_in").document(document.id).delete()
                        .addOnSuccessListener {
                            // Log success
                            Log.d("Check_In", "Check-in removed for user: $studentNumber")

                            // Update car counts in Firebase Realtime Database based on time value
                            when (time) {
                                "Morning" -> {
                                    // Decrement projectedMorningCars count in Firebase Realtime Database
                                    morningCarCountRef.get().addOnSuccessListener { snapshot ->
                                        val currentCount = snapshot.getValue(Int::class.java) ?: 0
                                        morningCarCountRef.setValue(currentCount - 1)
                                    }
                                }
                                "Afternoon" -> {
                                    // Decrement projectedAfternoonCars count in Firebase Realtime Database
                                    afternoonCarCountRef.get().addOnSuccessListener { snapshot ->
                                        val currentCount = snapshot.getValue(Int::class.java) ?: 0
                                        afternoonCarCountRef.setValue(currentCount - 1)
                                    }
                                }
                                else -> {
                                    Log.e("Check_In", "Unknown time value: $time")
                                }
                            }

                            // Handle time slot decrement
                            if (timeSlot != null) {
                                val timeSlotRef: DatabaseReference = database.getReference("timeSlots").child(timeSlot)
                                timeSlotRef.get().addOnSuccessListener { snapshot ->
                                    val currentCount = snapshot.getValue(Int::class.java) ?: 0
                                    // Decrement the time slot count if greater than 0
                                    if (currentCount > 0) {
                                        timeSlotRef.setValue(currentCount - 1)
                                    }
                                }
                            }

                            // Show toast message
                            Toast.makeText(context, "Check-In status removed successfully.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Check_In", "Error removing check-in", e)
                        }
                }
            } else {
                Log.d("Check_In", "No check-in found for user: $studentNumber")
                // Show toast message if no check-in found
                Toast.makeText(context, "No check-in record found to remove.", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener { e ->
            Log.e("Check_In", "Error fetching check-in", e)
        }
}

// Helper function to get the current date in DD/MM/YYYY format
fun getTodayDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date())
}

// Success Dialog
@Composable
fun SuccessDialog() {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Check-In Successful!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Green
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { /* Dismiss the dialog */ }) {
                    Text("OK")
                }
            }
        }
    }
}

// Already Checked-In Dialog
@Composable
fun AlreadyCheckedInDialog() {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "You have already checked in today.",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { }) {
                    Text("OK")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CheckInPagePreview() {
    val navController = rememberNavController()
    MaterialTheme {
        CheckInPage(navController)
    }
}
