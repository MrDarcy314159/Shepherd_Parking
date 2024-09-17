package za.varsitycollege.shepherd_parking

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    fun setLoggedIn(email: String) {
        prefs.edit().putBoolean("is_logged_in", true).apply()
        prefs.edit().putString("user_email", email).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    fun getLoggedInUserEmail(): String? {
        return prefs.getString("user_email", null)
    }

    fun clearLoginState() {
        prefs.edit().clear().apply()
    }
}