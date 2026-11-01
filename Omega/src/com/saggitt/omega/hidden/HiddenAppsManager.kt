package com.saggitt.omega.hidden

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object HiddenAppsManager {
    private const val PREFS_NAME = "hidden_apps_prefs"
    private const val KEY_HIDDEN_PACKAGES = "hidden_packages"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getHiddenPackages(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_HIDDEN_PACKAGES, emptySet()) ?: emptySet()
    }

    fun saveHiddenPackages(context: Context, packages: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_HIDDEN_PACKAGES, packages).apply()
    }

    fun isAppHidden(context: Context, packageName: String): Boolean {
        return getHiddenPackages(context).contains(packageName)
    }

    fun authenticate(activity: FragmentActivity, onSuccess: () -> Unit, onError: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError()
            }
        })

        val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Gizli Uygulamalar")
            .setSubtitle("Erişmek için kimliğinizi doğrulayın")
            .setAllowedAuthenticators(authenticators)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
