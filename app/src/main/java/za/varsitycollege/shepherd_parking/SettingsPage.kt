package za.varsitycollege.shepherd_parking

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(navController: NavController, userManager: UserManager) {
    var studentNumber by remember { mutableStateOf("ST10000001") } // Default value until fetched
    var facialRecognition by remember { mutableStateOf(false) }
    var fingerprintSettings by remember { mutableStateOf(false) }
    var locationServices by remember { mutableStateOf(false) }
    var pushNotifications by remember { mutableStateOf(false) }

    var expandedDropdown by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val languageManager = remember { LanguageManager(context) }
    var selectedLanguage by remember { mutableStateOf(languageManager.getLanguage()) }

    val languages = listOf("en" to "English", "af" to "Afrikaans", "zu" to "Zulu", "xh" to "Xhosa")

    // Fetch the student's number from userManager when the page loads
    LaunchedEffect(Unit) {
        val email = userManager.getCurrentUserEmail()
        email?.let {
            userManager.getStudentNumber(
                onSuccess = { fetchedStudentNumber ->
                    studentNumber = fetchedStudentNumber
                },
                onFailure = {
                    Toast.makeText(context, "Failed to load student number", Toast.LENGTH_SHORT).show()
                }
            )
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
                text = "Settings:",
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
                        text = "Student Information",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.DarkGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Student Number Input
                    OutlinedTextField(
                        value = studentNumber,
                        onValueChange = { studentNumber = it },
                        label = { Text("Student Number") },
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
                                    userManager.saveStudentNumber(it, studentNumber)
                                    Toast.makeText(context, "Student number updated successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Student number cannot be empty", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.MintGreen,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update Student Number")
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
                            Text("Fingerprint", color = AppColors.DarkGray)
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
                            Text("Facial Recognition", color = AppColors.DarkGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Location Services Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Location Services", color = AppColors.DarkGray)
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
                        Text("Push Notifications", color = AppColors.DarkGray)
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
                        Text("View App Permissions")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box {
                        OutlinedTextField(
                            value = languages.find { it.first == selectedLanguage }?.second ?: "English",
                            onValueChange = {},
                            label = { Text(stringResource(R.string.select_language)) },
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = if (expandedDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand Language Selection",
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
