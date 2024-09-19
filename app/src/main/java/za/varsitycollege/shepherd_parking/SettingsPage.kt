package za.varsitycollege.shepherd_parking

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(navController: NavController, userManager: UserManager) {
    var studentNumber by remember { mutableStateOf("ST10000001") } // Default value until fetched
    var oldStudentNumber by remember { mutableStateOf("") }
    var facialRecognition by remember { mutableStateOf(false) }
    var fingerprintSettings by remember { mutableStateOf(false) }
    var locationServices by remember { mutableStateOf(false) }
    var pushNotifications by remember { mutableStateOf(false) }

    var expandedDropdown by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }

    val languageManager = remember { LanguageManager(context) }
    var selectedLanguage by remember { mutableStateOf(languageManager.getLanguage()) }

    val languages = listOf("en" to "English", "af" to "Afrikaans", "zu" to "Zulu")

    // Show toast message
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            toastMessage = null
        }
    }

    // Fetch the student's number from userManager when the page loads
    LaunchedEffect(Unit) {
        val email = userManager.getCurrentUserEmail()
        email?.let {
            userManager.getStudentNumber(
                onSuccess = { fetchedStudentNumber ->
                    studentNumber = fetchedStudentNumber
                    oldStudentNumber = fetchedStudentNumber // Store old student number to update Firebase records
                },
                onFailure = {
                    toastMessage = context.getString(R.string.failed_to_load_student_number)
                }
            )
        }
    }

    // Function to update the student number in Firebase
    fun updateStudentNumberInFirebase(newStudentNumber: String) {
        if (newStudentNumber.isNotBlank() && newStudentNumber != oldStudentNumber) {
            // List of collections to update
            val collectionsToUpdate = listOf("check_in", "lecturers", "traffic_feedback")

            // Loop through each collection and update the student number
            for (collection in collectionsToUpdate) {
                db.collection(collection)
                    .whereEqualTo("stdNumber", oldStudentNumber)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val batch = db.batch()
                        for (document in querySnapshot.documents) {
                            val docRef = db.collection(collection).document(document.id)
                            batch.update(docRef, "stdNumber", newStudentNumber)
                        }
                        // Commit the batch for this collection
                        batch.commit()
                            .addOnSuccessListener {
                                Log.d("UpdateStudentNumber", "$collection updated successfully.")
                            }
                            .addOnFailureListener { e ->
                                Log.e("UpdateStudentNumber", "Failed to update $collection", e)
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("UpdateStudentNumber", "Failed to fetch documents from $collection", e)
                    }
            }

            // Show success message when done
            toastMessage = context.getString(R.string.student_number_updated_successfully)
            oldStudentNumber = newStudentNumber // Update the oldStudentNumber reference
        } else if (newStudentNumber.isBlank()) {
            toastMessage = context.getString(R.string.student_number_cannot_be_empty)
        } else {
            toastMessage = context.getString(R.string.student_number_same_as_old)
        }
    }


    // Function to open App Info screen
    fun openAppInfo() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    // Check location and notification permissions
    LaunchedEffect(Unit) {
        locationServices = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        pushNotifications = NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    // Observe lifecycle events to detect when the app resumes (comes back from App Info)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Recheck permissions when the app resumes
                locationServices = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                pushNotifications = NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // Clean up when the composable is disposed
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
            Spacer(modifier = Modifier.height(16.dp))

            // Settings Page Header
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.headlineMedium,
                color = AppColors.DarkGray,
                modifier = Modifier.padding(vertical = 0.dp)
            )

            Spacer(modifier = Modifier.height(5.dp))

            // Student Information Section with Update Button and other settings inside the same card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.student_information),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.DarkGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Student Number Input
                    OutlinedTextField(
                        value = studentNumber,
                        onValueChange = { studentNumber = it },
                        label = { Text(stringResource(R.string.student_number)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = AppColors.MintGreen,
                            unfocusedBorderColor = AppColors.DarkGray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Update Button
                    Button(
                        onClick = {
                            val email = userManager.getCurrentUserEmail()
                            email?.let {
                                if (studentNumber.isNotBlank()) {
                                    updateStudentNumberInFirebase(studentNumber)
                                    userManager.saveStudentNumber(it, studentNumber)
                                } else {
                                    toastMessage = context.getString(R.string.student_number_cannot_be_empty)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.MintGreen,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.update_student_number))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fingerprint and Facial Recognition Checkboxes inside the card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = fingerprintSettings,
                                onCheckedChange = { fingerprintSettings = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AppColors.MintGreen,
                                    uncheckedColor = AppColors.DarkGray
                                )
                            )
                            Text(stringResource(R.string.fingerprint), color = AppColors.DarkGray)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = facialRecognition,
                                onCheckedChange = { facialRecognition = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AppColors.MintGreen,
                                    uncheckedColor = AppColors.DarkGray
                                )
                            )
                            Text(stringResource(R.string.facial_recognition), color = AppColors.DarkGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Location Services Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.location_services), color = AppColors.DarkGray)
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = locationServices,
                            onCheckedChange = {
                                openAppInfo() // Redirect to App Info when toggled
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppColors.MintGreen,
                                uncheckedThumbColor = AppColors.DarkGray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Push Notifications Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.push_notifications), color = AppColors.DarkGray)
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = pushNotifications,
                            onCheckedChange = {
                                openAppInfo() // Redirect to App Info when toggled
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppColors.MintGreen,
                                uncheckedThumbColor = AppColors.DarkGray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // View App Permissions Button (opens App Info)
                    Button(
                        onClick = {
                            openAppInfo() // Opens the app info screen
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.MintGreen,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.view_app_permissions))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Language selection dropdown
                    Box {
                        OutlinedTextField(
                            value = languages.find { it.first == selectedLanguage }?.second ?: "English",
                            onValueChange = {},
                            label = { Text(stringResource(R.string.select_language)) },
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = if (expandedDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = stringResource(R.string.expand_language_selection),
                                    modifier = Modifier.clickable { expandedDropdown = !expandedDropdown }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = AppColors.MintGreen,
                                unfocusedBorderColor = AppColors.DarkGray
                            )
                        )

                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            languages.forEach { (code, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        selectedLanguage = code
                                        languageManager.setLanguage(code)
                                        expandedDropdown = false
                                        // Recreate the activity to apply language change
                                        (context as? MainActivity)?.recreate()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
