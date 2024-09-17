package za.varsitycollege.shepherd_parking

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

class BiometricActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkBiometricSupport()
    }

    private fun checkBiometricSupport() {
        val biometricManager = BiometricManager.from(this)

        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
            val executor: Executor = ContextCompat.getMainExecutor(this)
            val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Biometric authentication succeeded
                    Toast.makeText(this@BiometricActivity, "Authentication succeeded", Toast.LENGTH_SHORT).show()

                    // Send result back to LoginPage
                    val resultIntent = Intent()
                    resultIntent.putExtra("AUTH_SUCCESS", true)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@BiometricActivity, "Authentication failed", Toast.LENGTH_SHORT).show()

                    val resultIntent = Intent()
                    resultIntent.putExtra("AUTH_SUCCESS", false)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login using Biometric")
                .setSubtitle("Authenticate to proceed")
                .setNegativeButtonText("Cancel")
                .build()

            biometricPrompt.authenticate(promptInfo)
        } else {
            // No biometric support or no biometrics enrolled
            val resultIntent = Intent()
            resultIntent.putExtra("AUTH_SUCCESS", false)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
