package za.varsitycollege.shepherd_parking

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

open class UserManager(
    private val context: Context,
    val auth: FirebaseAuth = FirebaseAuth.getInstance(),  // Allow injection of FirebaseAuth
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()  // Allow injection of FirebaseFirestore
) {
    private val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    // Fetch student number from Firestore or use a default if user is a Google user without one
    fun getStudentNumber(onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        val email = auth.currentUser?.email
        if (email != null) {
            firestore.collection("users").document(email).get()
                .addOnSuccessListener { document ->
                    val studentNumber = document.getString("studentNumber") ?: "ST10000002" // Default student number for Google users
                    onSuccess(studentNumber)
                }
                .addOnFailureListener {
                    onFailure()
                }
        } else {
            onFailure()
        }
    }

    fun saveUser(firebaseUser: FirebaseUser?, name: String? = null, surname: String? = null, studentNumber: String? = null) {
        val email = firebaseUser?.email ?: return

        val displayName = firebaseUser?.displayName
        val firstName = name ?: displayName?.split(" ")?.getOrNull(0) ?: ""
        val lastName = surname ?: displayName?.split(" ")?.getOrNull(1) ?: ""

        with(sharedPreferences.edit()) {
            putString("${email}_name", firstName)
            putString("${email}_surname", lastName)
            putString("${email}_studentNumber", studentNumber)
            apply()
        }

        val userMap = hashMapOf(
            "email" to email,
            "name" to firstName,
            "surname" to lastName
        )

        if (studentNumber != null) {
            userMap["studentNumber"] = studentNumber
        }

        firestore.collection("users")
            .document(email)
            .set(userMap)
            .addOnSuccessListener {
                Log.d("UserManager", "User saved successfully")
            }
            .addOnFailureListener { e ->
                Log.w("UserManager", "Error saving user: ${e.message}")
            }
    }

    fun getUser(email: String): User? {
        // Retrieve user data from SharedPreferences
        val password = sharedPreferences.getString("${email}_password", null) ?: return null
        val name = sharedPreferences.getString("${email}_name", null) ?: return null
        val surname = sharedPreferences.getString("${email}_surname", null) ?: return null
        val studentNumber = sharedPreferences.getString("${email}_studentNumber", null) ?: return null
        return User(email, password, name, surname, studentNumber)
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    fun fetchUserDetails(email: String, callback: (User?) -> Unit) {
        firestore.collection("users").document(email).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Create a User object from Firestore data
                    val user = User(
                        email = email,
                        password = "",
                        name = document.getString("name") ?: "",
                        surname = document.getString("surname") ?: "",
                        studentNumber = document.getString("studentNumber") ?: ""
                    )
                    callback(user)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun convertFirebaseUser(firebaseUser: FirebaseUser, callback: (User?) -> Unit) {
        fetchUserDetails(firebaseUser.email ?: "") { user ->
            if (user != null) {
                callback(user)
            } else {
                val defaultUser = User(
                    email = firebaseUser.email ?: "",
                    password = "", // No password for Google users
                    name = firebaseUser.displayName?.split(" ")?.getOrNull(0) ?: "",
                    surname = firebaseUser.displayName?.split(" ")?.getOrNull(1) ?: "",
                    studentNumber = ""
                )
                saveUser(firebaseUser, defaultUser.name, defaultUser.surname, defaultUser.studentNumber)
                callback(defaultUser)
            }
        }
    }

    fun saveStudentNumber(email: String, studentNumber: String) {
        val editor = sharedPreferences.edit()
        editor.putString("${email}_studentNumber", studentNumber)
        editor.apply()

        // Explicitly cast the map to Map<String, Any>
        val userMap = mapOf("studentNumber" to studentNumber)

        firestore.collection("users")
            .document(email)
            .update(userMap)
            .addOnSuccessListener {
                Log.d("UserManager", "Student number updated successfully")
            }
            .addOnFailureListener { e ->
                Log.w("UserManager", "Error updating student number: ${e.message}")
            }
    }


    fun isStudentNumberMissing(email: String, callback: (Boolean) -> Unit) {
        firestore.collection("users")
            .document(email)
            .get()
            .addOnSuccessListener { document ->
                val studentNumber = document.getString("studentNumber")
                callback(studentNumber.isNullOrEmpty())
            }
            .addOnFailureListener {
                callback(true)
            }
    }

    fun arePushNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean("push_notifications_enabled", true) // Default to true
    }

    fun setPushNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("push_notifications_enabled", enabled).apply()
    }

}

data class User(
    val email: String,
    val password: String,
    val name: String,
    val surname: String,
    val studentNumber: String
)