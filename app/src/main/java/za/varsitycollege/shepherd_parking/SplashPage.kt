package za.varsitycollege.shepherd_parking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

//comment
@Composable
fun SplashPage(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(2000) // Simulate loading time, 2 seconds
        navController.navigate("signUp") // Navigate to the SignUp page
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.MintGreen),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sheep),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(150.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SHEPHERD PARKING",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
        }
    }
}


