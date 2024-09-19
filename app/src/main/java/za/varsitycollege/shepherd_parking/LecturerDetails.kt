package za.varsitycollege.shepherd_parking

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerDetailsPage() {
    val context = LocalContext.current
    var lecturerName by remember { mutableStateOf("") }
    var lecturerEmail by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    var studentNumber by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val userManager = UserManager(context)

    // Fetch student number when the composable is first launched
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val currentUserEmail = auth.currentUser?.email ?: ""
            userManager.fetchUserDetails(currentUserEmail) { user ->
                studentNumber = user?.studentNumber
            }
        }
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
                                text = stringResource(R.string.add_lecturer_details),
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
                    OutlinedTextField(
                        value = lecturerName,
                        onValueChange = { lecturerName = it },
                        label = { Text(stringResource(R.string.lecturer_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = AppColors.MintGreen,
                            unfocusedBorderColor = AppColors.DarkGray
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = lecturerEmail,
                        onValueChange = { lecturerEmail = it },
                        label = { Text(stringResource(R.string.lecturer_email)) },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = AppColors.MintGreen,
                            unfocusedBorderColor = AppColors.DarkGray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Buttons Column
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Button(
                                onClick = {
                                    // Clear the fields
                                    lecturerName = ""
                                    lecturerEmail = ""
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                border = BorderStroke(2.dp, AppColors.DarkGray)
                            ) {
                                Text(stringResource(R.string.clear), color = Color.Black)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (lecturerName.isNotBlank() && lecturerEmail.isNotBlank() && studentNumber != null) {
                                        // Save data to Firestore
                                        val lecturerData = hashMapOf(
                                            "name" to lecturerName,
                                            "email" to lecturerEmail,
                                            "stdNumber" to studentNumber
                                        )

                                        firestore.collection("lecturers")
                                            .add(lecturerData)
                                            .addOnSuccessListener {
                                                showSuccessDialog = true
                                            }
                                            .addOnFailureListener { e ->
                                                // Log error and show error message
                                                e.printStackTrace()
                                                showErrorDialog = true
                                            }
                                    } else {
                                        showErrorDialog = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                border = BorderStroke(2.dp, AppColors.DarkGray)
                            ) {
                                Text(stringResource(R.string.add), color = Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Lecturer Icon to the right of the buttons
                        Image(
                            painter = painterResource(id = R.drawable.lecturer_icon),
                            contentDescription = stringResource(R.string.lecturer_icon_description),
                            modifier = Modifier.size(90.dp),
                        )
                    }
                }
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        CustomDialog(
            onDismissRequest = { showSuccessDialog = false },
            message = stringResource(R.string.lecturer_added_successfully),
            backgroundColor = AppColors.MintGreen
        )
    }

    // Error Dialog
    if (showErrorDialog) {
        CustomDialog(
            onDismissRequest = { showErrorDialog = false },
            message = stringResource(R.string.failed_to_add_lecturer),
            backgroundColor = Color.Red
        )
    }
}

@Composable
fun CustomDialog(
    onDismissRequest: () -> Unit,
    message: String,
    backgroundColor: Color
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.DarkGray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = AppColors.DarkGray
                    ),
                    border = BorderStroke(2.dp, AppColors.DarkGray)
                ) {
                    Text(stringResource(R.string.ok))
                }
            }
        }
    }
}

