package za.varsitycollege.shepherd_parking

import ApiTestPage
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.messaging.FirebaseMessaging
import za.varsitycollege.shepherd_parking.UserManager
import za.varsitycollege.shepherd_parking.SettingsPage

// Define CompositionLocals for UserManager and LanguageManager
val LocalUserManager = compositionLocalOf<UserManager> { error("UserManager not provided") }
val LocalLanguageManager = compositionLocalOf<LanguageManager> { error("LanguageManager not provided") }


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageManager = LanguageManager(this)
        languageManager.setLanguage(languageManager.getLanguage())

        // Subscribe to FCM topic
        FirebaseMessaging.getInstance().subscribeToTopic("parking")
            .addOnCompleteListener { task ->
                val msg = if (task.isSuccessful) {
                    "Subscribed to parking topic"
                } else {
                    "Subscription failed"
                }
                Log.d("FCM", msg)
            }

        setContent {
            ShepherdParkingApp()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ShepherdParkingApp() {
        val navController = rememberNavController()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val userManager = remember { UserManager(context) }
        // Creating the UserManager instance
        val languageManager = remember { LanguageManager(context) }

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val routesWithoutTopBarAndDrawer = listOf("login", "newUser", "map_updates", "guard_house")
        val lightBlue = Color(0xFFE3F2FD)

        // Provide the UserManager globally through CompositionLocalProvider
        CompositionLocalProvider(
            LocalUserManager provides userManager,
            LocalLanguageManager provides languageManager
        ) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    if (currentRoute !in routesWithoutTopBarAndDrawer) {
                        ModalDrawerSheet {
                            Box(
                                modifier = Modifier
                                    .background(AppColors.MintGreen)
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.sheep),
                                        contentDescription = "App Icon",
                                        modifier = Modifier.size(80.dp),
                                        tint = Color.Unspecified
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Shepherd Parking",
                                        color = Color.Black,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                label = { Text("Home") },
                                selected = currentRoute == "home",
                                onClick = {
                                    navController.navigate("home")
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                label = { Text("Settings") },
                                selected = currentRoute == "settings",
                                onClick = {
                                    navController.navigate("settings")
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null
                                    )
                                },
                                label = { Text("Check In") },
                                selected = currentRoute == "check_in",
                                onClick = {
                                    navController.navigate("check_in")
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Share, contentDescription = null) },
                                label = { Text("Traffic Feedback") },
                                selected = currentRoute == "traffic_feedback",
                                onClick = {
                                    navController.navigate("traffic_feedback")
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Email, contentDescription = null) },
                                label = { Text("Late") },
                                selected = currentRoute == "late",
                                onClick = {
                                    navController.navigate("late")
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                label = { Text("Analytics") },
                                selected = currentRoute == "analytics",
                                onClick = {
                                    navController.navigate("analytics")
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null
                                    )
                                },
                                label = { Text("Map Updates") },
                                selected = currentRoute == "map_updates",
                                onClick = {
                                    navController.navigate("map_updates")
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Face, contentDescription = null) },
                                label = { Text("Guard House") },
                                selected = false,
                                onClick = {
                                    navController.navigate("guard_house") {
                                        popUpTo(0)
                                    }
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                label = { Text("Logout") },
                                selected = false,
                                onClick = {
                                    navController.navigate("signUp") {
                                        popUpTo(0)
                                    }
                                    scope.launch { drawerState.close() }
                                }
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                label = { Text("API Test") },
                                selected = false,
                                onClick = {
                                    navController.navigate("api_test") {
                                        popUpTo(0)
                                    }
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        if (currentRoute !in routesWithoutTopBarAndDrawer) {
                            TopAppBar(
                                title = { Text("Menu") },
                                navigationIcon = {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = lightBlue,
                                    titleContentColor = Color.Black,
                                    navigationIconContentColor = Color.Black
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppColors.MintGreen)
                            .padding(innerPadding)
                    ) {
                        NavHost(navController = navController, startDestination = "signUp") {
                            composable("signUp") { SignUpPage(navController) }
                            composable("login") { LoginPage(navController) }
                            composable("newUser") { NewUserPage(navController) }
                            //UPDATED This
                            composable("home") {
                                HomePage(
                                    navController,
                                    userManager = LocalUserManager.current
                                )
                            }
                            composable("settings") { SettingsPage(navController,userManager) }
                            composable("check_in") { CheckInPage(navController) }

                            //UPDATED This
                            composable("traffic_feedback") {
                                TrafficFeedbackPage(
                                    navController,
                                    userManager
                                )
                            }

                            composable("late") { Late_Page(navController) }
                            composable("analytics") { AnalyticsPage(navController) }
                            composable("map_updates") { MapUpdatesPage(navController) }
                            composable("guard_house") { GuardHousePage(navController) }
                            composable("lecturer_details") { LecturerDetailsPage() }
                            composable("api_test") { ApiTestPage(navController) }
                        }
                    }
                }
            }
        }
    }
}

