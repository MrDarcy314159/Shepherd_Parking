package za.varsitycollege.shepherd_parking

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class FeedbackMarker(
    val position: LatLng,
    val title: String,
    val snippet: String,
    val timestamp: Long,
    val category: FeedbackCategory
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapUpdatesPage(navController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val firestore: FirebaseFirestore = Firebase.firestore

    var feedbackMarkers by remember { mutableStateOf<List<FeedbackMarker>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val varsityCollege = LatLng(-26.0932626, 28.0480246)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(varsityCollege, 12f)
    }

    LaunchedEffect(Unit) {
        try {
            val twentyFourHoursAgo = System.currentTimeMillis() - 24 * 60 * 60 * 1000
            val feedbackDocs = firestore.collection("traffic_feedback")
                .whereGreaterThan("timestamp", twentyFourHoursAgo)
                .get()
                .await()
                .documents

            feedbackMarkers = feedbackDocs.mapNotNull { doc ->
                try {
                    val location = doc.getString("location")
                    val message = doc.getString("message") ?: "No message"
                    val studentNumber = doc.getString("studentNumber") ?: "Unknown"
                    val timestamp = doc.getLong("timestamp") ?: return@mapNotNull null
                    val category = doc.getString("category")?.let { FeedbackCategory.valueOf(it) } ?: FeedbackCategory.Other

                    if (location == null) {
                        Log.w("MapUpdatesPage", "Skipping feedback entry due to null location: $doc")
                        return@mapNotNull null
                    }

                    val latLng = when {
                        location.startsWith("HARDCODED_LOCATION:") -> {
                            val (lat, lng) = location.removePrefix("HARDCODED_LOCATION:").split(",")
                            LatLng(lat.toDouble(), lng.toDouble())
                        }
                        location.contains(",") -> {
                            val (lat, lng) = location.split(",")
                            LatLng(lat.toDouble(), lng.toDouble())
                        }
                        else -> {
                            Log.w("MapUpdatesPage", "Invalid location format: $location")
                            return@mapNotNull null
                        }
                    }

                    FeedbackMarker(
                        position = latLng,
                        title = "Feedback from $studentNumber",
                        snippet = message,
                        timestamp = timestamp,
                        category = category
                    )
                } catch (e: Exception) {
                    Log.e("MapUpdatesPage", "Error parsing feedback entry: ${e.message}", e)
                    null
                }
            }
            isLoading = false
        } catch (e: Exception) {
            Log.e("MapUpdatesPage", "Error loading feedback: ${e.message}", e)
            errorMessage = "Error loading feedback: ${e.message}"
            isLoading = false
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // ... (keep the existing drawer content)
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Menu") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFE3F2FD),
                        titleContentColor = Color.Black,
                        navigationIconContentColor = Color.Black
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.MintGreen)
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header Bubble
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                        text = "Map Updates (Last 24 Hours)",
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

                    // Map Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                    ) {
                        when {
                            isLoading -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                            errorMessage != null -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                                }
                            }
                            feedbackMarkers.isEmpty() -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No feedback data available for the last 24 hours")
                                }
                            }
                            else -> {
                                GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = cameraPositionState
                                ) {
                                    Marker(
                                        state = MarkerState(position = varsityCollege),
                                        title = "Varsity College Sandton"
                                    )
                                    feedbackMarkers.forEach { marker ->
                                        Marker(
                                            state = MarkerState(position = marker.position),
                                            title = marker.title,
                                            snippet = marker.snippet,
                                            icon = BitmapDescriptorFactory.defaultMarker(getCategoryColor(marker.category))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Legend
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Legend",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            FeedbackCategory.values().forEach { category ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(getCategoryColor(category).toColor(), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(category.name)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getCategoryColor(category: FeedbackCategory): Float {
    return when (category) {
        FeedbackCategory.Accident -> BitmapDescriptorFactory.HUE_RED
        FeedbackCategory.HeavyTraffic -> BitmapDescriptorFactory.HUE_ORANGE
        FeedbackCategory.RoadConstruction -> BitmapDescriptorFactory.HUE_YELLOW
        FeedbackCategory.Other -> BitmapDescriptorFactory.HUE_AZURE
    }
}

fun Float.toColor(): Color {
    return when (this) {
        BitmapDescriptorFactory.HUE_RED -> Color.Red
        BitmapDescriptorFactory.HUE_ORANGE -> Color(0xFFFFA500)
        BitmapDescriptorFactory.HUE_YELLOW -> Color.Yellow
        BitmapDescriptorFactory.HUE_AZURE -> Color(0xFF007FFF)
        else -> Color.Gray
    }
}