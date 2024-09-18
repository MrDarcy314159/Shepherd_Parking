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
import androidx.compose.ui.res.stringResource
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

val LocalUserManager = compositionLocalOf<UserManager> { error("UserManager not provided") }
val LocalLanguageManager = compositionLocalOf<LanguageManager> { error("LanguageManager not provided") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageManager = LanguageManager(this)
        languageManager.setLanguage(languageManager.getLanguage())

        FirebaseMessaging.getInstance().subscribeToTopic("parking")
            .addOnCompleteListener { task ->
                val msg = if (task.isSuccessful) {
                    getString(R.string.subscribed_to_parking_topic)
                } else {
                    getString(R.string.subscription_failed)
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
        val languageManager = remember { LanguageManager(context) }

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val routesWithoutTopBarAndDrawer = listOf("login", "newUser", "map_updates", "guard_house", "signUp")
        val lightBlue = Color(0xFFE3F2FD)

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
                                        contentDescription = stringResource(R.string.app_icon_description),
                                        modifier = Modifier.size(80.dp),
                                        tint = Color.Unspecified
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.app_name),
                                        color = Color.Black,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                label = { Text(stringResource(R.string.home)) },
                                selected = currentRoute == "home",
                                onClick = {
                                    navController.navigate("home")
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                label = { Text(stringResource(R.string.settings)) },
                                selected = currentRoute == "settings",
                                onClick = {
                                    navController.navigate("settings")
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                                label = { Text(stringResource(R.string.check_in)) },
                                selected = currentRoute == "check_in",
                                onClick = {
                                    navController.navigate("check_in")
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Share, contentDescription = null) },
                                label = { Text(stringResource(R.string.traffic_feedback)) },
                                selected = currentRoute == "traffic_feedback",
                                onClick = {
                                    navController.navigate("traffic_feedback")
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Email, contentDescription = null) },
                                label = { Text(stringResource(R.string.late)) },
                                selected = currentRoute == "late",
                                onClick = {
                                    navController.navigate("late")
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                label = { Text(stringResource(R.string.analytics)) },
                                selected = currentRoute == "analytics",
                                onClick = {
                                    navController.navigate("analytics")
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                label = { Text(stringResource(R.string.map_updates)) },
                                selected = currentRoute == "map_updates",
                                onClick = {
                                    navController.navigate("map_updates")
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                label = { Text(stringResource(R.string.logout)) },
                                selected = false,
                                onClick = {
                                    navController.navigate("signUp") {
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
                                title = { Text(stringResource(R.string.menu)) },
                                navigationIcon = {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu))
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
                            composable("home") { HomePage(navController, userManager = LocalUserManager.current) }
                            composable("settings") { SettingsPage(navController, userManager) }
                            composable("check_in") { CheckInPage(navController) }
                            composable("traffic_feedback") { TrafficFeedbackPage(navController, userManager) }
                            composable("late") { Late_Page(navController) }
                            composable("analytics") { AnalyticsPage(navController) }
                            composable("map_updates") { MapUpdatesPage(navController) }
                            composable("guard_house") { GuardHousePage(navController) }
                            composable("lecturer_details") { LecturerDetailsPage() }
                        }
                    }
                }
            }
        }
    }
}
