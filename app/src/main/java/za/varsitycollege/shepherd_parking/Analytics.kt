package za.varsitycollege.shepherd_parking

import android.graphics.Canvas
import android.util.Log
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await
import me.bytebeats.views.charts.bar.BarChart
import me.bytebeats.views.charts.bar.BarChartData
import me.bytebeats.views.charts.bar.render.bar.SimpleBarDrawer
import me.bytebeats.views.charts.bar.render.label.SimpleLabelDrawer
import me.bytebeats.views.charts.bar.render.xaxis.SimpleXAxisDrawer
import me.bytebeats.views.charts.bar.render.yaxis.SimpleYAxisDrawer
import me.bytebeats.views.charts.simpleChartAnimation
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.Paint
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import me.bytebeats.views.charts.bar.render.xaxis.IXAxisDrawer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsPage(navController: NavController) {
    // Variables to store the car count and max car count
    var morningCarCount by remember { mutableStateOf(0) }
    var afternoonCarCount by remember { mutableStateOf(0) }
    var maxCarCount by remember { mutableStateOf(100) }
    var morningProgress by remember { mutableStateOf(0f) }
    var afternoonProgress by remember { mutableStateOf(0f) }

    // Firebase Database reference
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val morningCarCountRef = database.getReference("projectedMorningCars")
    val afternoonCarCountRef = database.getReference("projectedAfternoonCars")
    val maxCarCountRef = database.getReference("maxCarCount")

    // Fetch morning car count from Firebase
    morningCarCountRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            morningCarCount = snapshot.getValue(Int::class.java) ?: 0
            morningProgress = if (maxCarCount > 0) morningCarCount.toFloat() / maxCarCount.toFloat() else 0f
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Failed to read carCount: ${error.message}")
        }
    })

    // Fetch afternoon car count from Firebase
    afternoonCarCountRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            afternoonCarCount = snapshot.getValue(Int::class.java) ?: 0
            afternoonProgress = if (maxCarCount > 0) afternoonCarCount.toFloat() / maxCarCount.toFloat() else 0f
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Failed to read carCount: ${error.message}")
        }
    })

    // Fetch max car count from Firebase
    maxCarCountRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            maxCarCount = snapshot.getValue(Int::class.java) ?: 100
            morningProgress = if (maxCarCount > 0) morningCarCount.toFloat() / maxCarCount.toFloat() else 0f
            afternoonProgress = if (maxCarCount > 0) afternoonCarCount.toFloat() / maxCarCount.toFloat() else 0f
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Failed to read maxCarCount: ${error.message}")
        }
    })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.MintGreen)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
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
                            text = "Analytics",
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

        // Main Content Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Projected Capacity for Tomorrow (Morning)
                Text(
                    text = "Projected Capacity for Tomorrow Morning",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.DarkGray
                )
                Text(
                    text = "${(morningProgress * 100).toInt()}% FULL",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = morningProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = AppColors.MintGreen,
                    trackColor = Color.LightGray
                )

                // Projected Capacity for Tomorrow (Afternoon)
                Text(
                    text = "Projected Capacity for Tomorrow Afternoon",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.DarkGray
                )
                Text(
                    text = "${(afternoonProgress * 100).toInt()}% FULL",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = afternoonProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = AppColors.MintGreen,
                    trackColor = Color.LightGray
                )

                // Day-wise Analytics Chart
                Text(
                    text = "Projected capacity for tomorrow",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                BarChartView()
            }
        }
        // Varsity College logo outside of the main content card
        Image(
            painter = painterResource(id = R.drawable.varsity_college_icon),
            contentDescription = "Varsity College Logo",
            modifier = Modifier
                .padding(top = 12.dp, bottom = 16.dp)
                .height(50.dp)
        )
    }
}

@Composable
fun BarChartView() {
    // Firebase Database reference
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    // Time slots references
    val timeSlotReferences = listOf(
        database.getReference("timeSlots/sevenToEight"),
        database.getReference("timeSlots/eightToNine"),
        database.getReference("timeSlots/nineToTen"),
        database.getReference("timeSlots/tenToEleven"),
        database.getReference("timeSlots/elevenToTwelve"),
        database.getReference("timeSlots/twelveToOne"),
        database.getReference("timeSlots/oneToTwo"),
        database.getReference("timeSlots/twoToThree"),
        database.getReference("timeSlots/threeToFour"),
        database.getReference("timeSlots/fourToFive")
    )

    // Remember the time slots state
    val timeSlots = remember { List(10) { mutableStateOf(0) } }

    // Load initial car count from Firebase
    LaunchedEffect(Unit) {
        // Load time slots from Firebase
        timeSlotReferences.forEachIndexed { index, ref ->
            val snapshot = ref.get().await()
            timeSlots[index].value = snapshot.getValue(Int::class.java) ?: 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BarChart(
            barChartData = BarChartData(
                bars = timeSlots.mapIndexed { index, state ->
                    BarChartData.Bar(
                        label = "${state.value}",
                        value = state.value.toFloat(),
                        color = AppColors.MintGreen
                    )
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            animation = simpleChartAnimation(),
            barDrawer = SimpleBarDrawer(),
            xAxisDrawer = SimpleXAxisDrawer(),
            yAxisDrawer = SimpleYAxisDrawer(labelTextColor = Color.Transparent)
        )

        Text(
            text = "   7am                                          11am                                           3pm",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = "Time Slots",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.DarkGray
        )
    }
}


@Preview(showBackground = true)
@Composable
fun AnalyticsPagePreview() {
    val navController = rememberNavController()
    MaterialTheme {
        AnalyticsPage(navController)
    }
}


