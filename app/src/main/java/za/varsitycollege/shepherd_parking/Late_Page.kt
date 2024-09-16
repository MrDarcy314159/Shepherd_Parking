package za.varsitycollege.shepherd_parking

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Late_Page(navController: NavController) {
    val context = LocalContext.current
    var studentNumber by remember { mutableStateOf("ST10000001") }
    var selectedLecturer by remember { mutableStateOf("Select Lecturer") }
    var selectedReason by remember { mutableStateOf("Select Reason") }
    var extraInformation by remember { mutableStateOf("") }
    var lecturerDropdownExpanded by remember { mutableStateOf(false) }
    var reasonDropdownExpanded by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var lecturers by remember { mutableStateOf(listOf<String>()) }
    var lecturerEmail by remember { mutableStateOf("") }  // State to store lecturer's email

    val firestore: FirebaseFirestore = Firebase.firestore
    val userManager = UserManager(context)

    // Fetch student number and lecturers
    LaunchedEffect(Unit) {
        userManager.getStudentNumber(onSuccess = { fetchedStudentNumber ->
            studentNumber = fetchedStudentNumber
            // Fetch lecturers based on the student number
            firestore.collection("lecturers")
                .whereEqualTo("studentNumber", studentNumber)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    lecturers = querySnapshot.documents.mapNotNull { it.getString("name") }
                }
        }, onFailure = {
            // Handle failure in fetching student number
            Toast.makeText(context, "Failed to fetch student number", Toast.LENGTH_SHORT).show()
        })
    }

    val reasons = listOf("Traffic", "Accident", "Other")

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
                                text = "Late",
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
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.robot_icon),
                        contentDescription = "Robot Icon",
                        modifier = Modifier.size(80.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Add Lecturer Button
                    OutlinedButton(
                        onClick = { navController.navigate("lecturer_details") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.DarkGray)
                    ) {
                        Text("Add Lecturer")
                    }

                    // Lecturer Dropdown
                    ExposedDropdownMenuBox(
                        expanded = lecturerDropdownExpanded,
                        onExpandedChange = { lecturerDropdownExpanded = !lecturerDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedLecturer,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Lecturer") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = lecturerDropdownExpanded)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                focusedBorderColor = AppColors.MintGreen,
                                unfocusedBorderColor = AppColors.DarkGray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = lecturerDropdownExpanded,
                            onDismissRequest = { lecturerDropdownExpanded = false }
                        ) {
                            lecturers.forEach { lecturer ->
                                DropdownMenuItem(
                                    text = { Text(lecturer) },
                                    onClick = {
                                        selectedLecturer = lecturer
                                        lecturerDropdownExpanded = false

                                        // Fetch lecturer email from Firestore when selected
                                        firestore.collection("lecturers")
                                            .whereEqualTo("name", lecturer)
                                            .get()
                                            .addOnSuccessListener { querySnapshot ->
                                                val lecturerDoc = querySnapshot.documents.firstOrNull()
                                                lecturerEmail = lecturerDoc?.getString("email") ?: ""
                                            }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Reason Dropdown
                    ExposedDropdownMenuBox(
                        expanded = reasonDropdownExpanded,
                        onExpandedChange = { reasonDropdownExpanded = !reasonDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedReason,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Reason") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = reasonDropdownExpanded)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                focusedBorderColor = AppColors.MintGreen,
                                unfocusedBorderColor = AppColors.DarkGray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = reasonDropdownExpanded,
                            onDismissRequest = { reasonDropdownExpanded = false }
                        ) {
                            reasons.forEach { reason ->
                                DropdownMenuItem(
                                    text = { Text(reason) },
                                    onClick = {
                                        selectedReason = reason
                                        reasonDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Extra Information TextBox
                    OutlinedTextField(
                        value = extraInformation,
                        onValueChange = { extraInformation = it },
                        label = { Text("Extra Information") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(75.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = AppColors.MintGreen,
                            unfocusedBorderColor = AppColors.DarkGray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Button(
                                onClick = {
                                    // Clear the fields
                                    selectedLecturer = "Select Lecturer"
                                    selectedReason = "Select Reason"
                                    extraInformation = ""
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = AppColors.DarkGray
                                ),
                                border = BorderStroke(2.dp, AppColors.DarkGray)
                            ) {
                                Text("Clear")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (selectedLecturer != "Select Lecturer" &&
                                        selectedReason != "Select Reason" &&
                                        extraInformation.isNotBlank()
                                    ) {
                                        // Save data to Firestore
                                        val lateData = hashMapOf(
                                            "studentNumber" to studentNumber,
                                            "lecturer" to selectedLecturer,
                                            "reason" to selectedReason,
                                            "extraInformation" to extraInformation,
                                            "timestamp" to System.currentTimeMillis()
                                        )

                                        firestore.collection("late_submissions")
                                            .add(lateData)
                                            .addOnSuccessListener {
                                                showSuccessDialog = true
                                                // Create email intent using ACTION_SEND
                                                val emailIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_EMAIL, arrayOf(lecturerEmail)) // Use the lecturer's email
                                                    putExtra(Intent.EXTRA_SUBJECT, "Late Submission")
                                                    putExtra(
                                                        Intent.EXTRA_TEXT,
                                                        "Student Number: $studentNumber\n" +
                                                                "Lecturer: $selectedLecturer\n" +
                                                                "Reason: $selectedReason\n" +
                                                                "Extra Information: $extraInformation"
                                                    )
                                                }
                                                // Use a chooser to allow the user to select the app
                                                if (emailIntent.resolveActivity(context.packageManager) != null) {
                                                    context.startActivity(
                                                        Intent.createChooser(emailIntent, "Choose an email app")
                                                    )
                                                } else {
                                                    // Display a toast message if no email app is available
                                                    Toast.makeText(
                                                        context,
                                                        "No email app found. Please install an email app to send this message.",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                            .addOnFailureListener {
                                                // Handle failure
                                            }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = AppColors.DarkGray
                                ),
                                border = BorderStroke(2.dp, AppColors.DarkGray)
                            ) {
                                Text("Submit")
                            }
                        }
                    }
                }
            }

            // Success Dialog
            if (showSuccessDialog) {
                Dialog(onDismissRequest = { showSuccessDialog = false }) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Success!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.DarkGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your late submission has been recorded.",
                                fontSize = 16.sp,
                                color = AppColors.DarkGray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showSuccessDialog = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.MintGreen,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("OK")
                            }
                        }
                    }
                }
            }
        }
    }
}
