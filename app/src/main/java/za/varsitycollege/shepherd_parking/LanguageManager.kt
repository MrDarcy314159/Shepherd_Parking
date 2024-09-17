package za.varsitycollege.shepherd_parking

import android.content.Context
import android.content.res.Configuration
import java.util.*

class LanguageManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("LanguagePrefs", Context.MODE_PRIVATE)

    fun setLanguage(languageCode: String) {
        prefs.edit().putString("language", languageCode).apply()
        updateResources(languageCode)
    }

    fun getLanguage(): String {
        return prefs.getString("language", "en") ?: "en"
    }

    private fun updateResources(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        context.createConfigurationContext(configuration)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}