package za.varsitycollege.shepherd_parking

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun SignUpPage(navController: NavController) {
    var carCount by remember { mutableStateOf(0) }
    var maxCarCount by remember { mutableStateOf(100) }
    var progress by remember { mutableStateOf(0f) }

    var showDialog by remember { mutableStateOf(false) }
    var googleAccount by remember { mutableStateOf<GoogleSignInAccount?>(null) }

    // Initialize UserManager
    val context = LocalContext.current
    val userManager = remember { UserManager(context) }

    // Firebase Realtime Database reference
    val database = FirebaseDatabase.getInstance()
    val carCountRef = database.getReference("carCount")
    val maxCarCountRef = database.getReference("maxCarCount")

    // Fetch car count from Firebase
    carCountRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            carCount = snapshot.getValue(Int::class.java) ?: 0
            progress = if (maxCarCount > 0) carCount.toFloat() / maxCarCount.toFloat() else 0f
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Failed to read carCount: ${error.message}")
        }
    })

    maxCarCountRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            maxCarCount = snapshot.getValue(Int::class.java) ?: 100
            progress = if (maxCarCount > 0) carCount.toFloat() / maxCarCount.toFloat() else 0f
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Failed to read maxCarCount: ${error.message}")
        }
    })

    val auth = remember { FirebaseAuth.getInstance() }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("752353533147-1vkekoov3afqtft357ulkq5bdk1uq6f1.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    fun signInWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let { user ->
                        // Convert Google user to app User and save their details
                        userManager.convertFirebaseUser(user) { appUser: User? ->
                            if (appUser != null) {
                                // Prompt for student number if needed, or proceed as usual
                                if (appUser.studentNumber.isEmpty()) {
                                    // Prompt user to enter their student number
                                }
                            }
                        }
                    }
                    navController.navigate("home") {
                        popUpTo("signUp") { inclusive = true }
                    }
                } else {
                    Log.w("SignUpPage", "signInWithCredential:failure", signInTask.exception)
                    Toast.makeText(context, "Google Sign-In Failed: ${signInTask.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun handleGoogleSignInResult(account: GoogleSignInAccount) {
        auth.fetchSignInMethodsForEmail(account.email!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods.isNullOrEmpty()) {
                        signInWithGoogle(account)
                    } else {
                        googleAccount = account
                        showDialog = true
                    }
                } else {
                    Log.w("SignUpPage", "fetchSignInMethodsForEmail:failure", task.exception)
                    Toast.makeText(context, "Error checking email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.let {
                    handleGoogleSignInResult(it)
                }
            } catch (e: ApiException) {
                Log.w("SignUpPage", "Google sign in failed", e)
            }
        }
    }

    if (showDialog) {
        ShowEmailExistsDialog(
            onSignInConfirmed = {
                showDialog = false
                googleAccount?.let { account ->
                    signInWithGoogle(account)
                }
            },
            onCancel = {
                showDialog = false
                googleAccount = null
            }
        )
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

            Box(
                modifier = Modifier
                    .size(175.dp)
                    .clip(CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sheep),
                    contentDescription = stringResource(R.string.sheep_logo_description),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.parking_status),
                        fontWeight = FontWeight.Bold,
                        color = AppColors.DarkGray
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth(),
                        color = AppColors.MintGreen,
                        trackColor = Color.LightGray
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.parking_full_percentage, (progress * 100).toInt()),
                        fontWeight = FontWeight.Bold,
                        color = AppColors.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { navController.navigate("newUser") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.MintGreen)
                    ) {
                        Text(
                            text = stringResource(R.string.sign_up),
                            fontWeight = FontWeight.Bold,
                            color = AppColors.DarkGray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.or),
                        fontWeight = FontWeight.Bold,
                        color = AppColors.DarkGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { navController.navigate("login") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.MintGreen)
                    ) {
                        Text(
                            text = stringResource(R.string.login),
                            fontWeight = FontWeight.Bold,
                            color = AppColors.DarkGray
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { launcher.launch(googleSignInClient.signInIntent) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .background(Color.White),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.google_logo),
                                contentDescription = stringResource(R.string.google_logo_description),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.continue_with_google),
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Image(
            painter = painterResource(id = R.drawable.varsity_college_icon),
            contentDescription = stringResource(R.string.varsity_college_logo_description),
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

@Composable
fun ShowEmailExistsDialog(
    onSignInConfirmed: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = stringResource(R.string.account_exists_title))
        },
        text = {
            Text(stringResource(R.string.account_exists_message))
        },
        confirmButton = {
            Button(onClick = onSignInConfirmed) {
                Text(stringResource(R.string.sign_in))
            }
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SignUpPagePreview() {
    val navController = rememberNavController()
    MaterialTheme {
        SignUpPage(navController)
    }
}