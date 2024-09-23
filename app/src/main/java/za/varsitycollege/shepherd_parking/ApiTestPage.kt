import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import za.varsitycollege.shepherd_parking.AppColors
import za.varsitycollege.shepherd_parking.R

@Composable
fun ApiTestPage(navController: NavController) {
    // Variables to store API data
    var apiData by remember { mutableStateOf("Loading...") }
    var inputText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var responseMessage by remember { mutableStateOf("") }

    // Firebase Database reference
    val database = FirebaseDatabase.getInstance()
    val apiDataRef: DatabaseReference = database.getReference("apiData")

    // Fetch API data from Firebase
    LaunchedEffect(Unit) {
        apiDataRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                apiData = snapshot.getValue(String::class.java) ?: "No data"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to read apiData: ${error.message}")
            }
        })
    }

    val coroutineScope = rememberCoroutineScope()

    // Function to post data to Vercel endpoint
    suspend fun postDataToEndpoint(text: String) {
        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType() // Use application/json if your API expects JSON
        val jsonBody = """
        {
            "data": "$text"
        }
    """.trimIndent()
        val body = jsonBody.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://shepherd-parking-api.vercel.app/api/convert")  // Replace with your Vercel endpoint
            .post(body)
            .build()

        try {
            val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "No response body"
                Log.d("API", "Response: $responseBody")
                responseMessage = "Success: $responseBody"
            } else {
                val errorBody = response.body?.string() ?: "No error body"
                Log.e("API", "Error: ${response.message}")
                responseMessage = "Error: ${response.message} - $errorBody"
            }
        } catch (e: Exception) {
            Log.e("API", "Exception: ${e.message}")
            responseMessage = "Exception: ${e.message}"
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
            // Header Bubble
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                text = "API TEST PAGE",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.DarkGray
                            )
                            Text(
                                text = "API Data",
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

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "API Data:",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = apiData,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("Enter text to send") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isSubmitting = true
                                // Post data to Vercel endpoint
                                postDataToEndpoint(inputText)
                                isSubmitting = false
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = responseMessage,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        val context = LocalContext.current

        Image(
            painter = painterResource(id = R.drawable.varsity_college_icon),
            contentDescription = "Varsity College Logo",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .height(50.dp)
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.varsitycollege.co.za/"))
                    context.startActivity(intent)
                }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ApiTestPagePreview() {
    val navController = rememberNavController()
    MaterialTheme {
        ApiTestPage(navController)
    }
}
