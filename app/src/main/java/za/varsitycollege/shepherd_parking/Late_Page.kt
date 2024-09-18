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
import androidx.compose.ui.res.stringResource
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
    var selectedLecturer by remember { mutableStateOf("") }
    var selectedReason by remember { mutableStateOf("") }
    var extraInformation by remember { mutableStateOf("") }
    var lecturerDropdownExpanded by remember { mutableStateOf(false) }
    var reasonDropdownExpanded by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var lecturers by remember { mutableStateOf(listOf<String>()) }
    var lecturerEmail by remember { mutableStateOf("") }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val firestore: FirebaseFirestore = Firebase.firestore
    val userManager = UserManager(context)

    val selectLecturerString = stringResource(R.string.select_lecturer)
    val selectReasonString = stringResource(R.string.select_reason)

    // Initialize selectedLecturer and selectedReason
    LaunchedEffect(Unit) {
        selectedLecturer = selectLecturerString
        selectedReason = selectReasonString
    }

    // Show toast message
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            toastMessage = null
        }
    }

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
            toastMessage = context.getString(R.string.failed_to_fetch_student_number)
        })
    }

    val reasons = listOf(
        stringResource(R.string.reason_traffic),
        stringResource(R.string.reason_accident),
        stringResource(R.string.reason_other)
    )

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
                                text = stringResource(R.string.late),
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
                        contentDescription = stringResource(R.string.robot_icon_description),
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
                        Text(stringResource(R.string.add_lecturer))
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
                            label = { Text(stringResource(R.string.lecturer)) },
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
                            label = { Text(stringResource(R.string.reason)) },
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
                        label = { Text(stringResource(R.string.extra_information)) },
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
                                    selectedLecturer = selectLecturerString
                                    selectedReason = selectReasonString
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
                                Text(stringResource(R.string.clear))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (selectedLecturer != selectLecturerString &&
                                        selectedReason != selectReasonString &&
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
                                                    putExtra(Intent.EXTRA_EMAIL, arrayOf(lecturerEmail))
                                                    putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.late_submission_subject))
                                                    putExtra(
                                                        Intent.EXTRA_TEXT,
                                                        context.getString(
                                                            R.string.late_submission_email_body,
                                                            studentNumber,
                                                            selectedLecturer,
                                                            selectedReason,
                                                            extraInformation
                                                        )
                                                    )
                                                }
                                                // Use a chooser to allow the user to select the app
                                                if (emailIntent.resolveActivity(context.packageManager) != null) {
                                                    context.startActivity(
                                                        Intent.createChooser(emailIntent, context.getString(R.string.choose_email_app))
                                                    )
                                                } else {
                                                    toastMessage = context.getString(R.string.no_email_app_found)
                                                }
                                            }
                                            .addOnFailureListener {
                                                toastMessage = context.getString(R.string.failed_to_submit_late_notification)
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
                                Text(stringResource(R.string.submit))
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
                                text = stringResource(R.string.success),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.DarkGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.late_submission_recorded),
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
                                Text(stringResource(R.string.ok))
                            }
                        }
                    }
                }
            }
        }
    }
}