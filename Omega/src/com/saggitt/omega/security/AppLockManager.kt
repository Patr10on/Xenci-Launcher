package com.saggitt.omega.security

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricManager
import com.android.launcher3.R
import com.android.launcher3.util.Executors.MAIN_EXECUTOR
import java.security.MessageDigest

object AppLockManager {
    private const val PREFS_NAME = "omega_app_lock_prefs"
    private const val KEY_PIN_HASH = "key_app_lock_pin_hash"
    private const val KEY_BIOMETRIC_ENABLED = "key_app_lock_biometric_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(pin.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun hasCustomPin(context: Context): Boolean {
        return !getPrefs(context).getString(KEY_PIN_HASH, null).isNullOrEmpty()
    }

    fun setCustomPin(context: Context, pin: String?) {
        if (pin.isNullOrEmpty()) {
            getPrefs(context).edit().remove(KEY_PIN_HASH).apply()
        } else {
            getPrefs(context).edit().putString(KEY_PIN_HASH, hashPin(pin)).apply()
        }
    }

    fun verifyPin(context: Context, input: String): Boolean {
        val savedHash = getPrefs(context).getString(KEY_PIN_HASH, null) ?: return false
        return savedHash == hashPin(input)
    }

    fun isBiometricEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BIOMETRIC_ENABLED, true)
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isBiometricHardwareAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun getActivity(context: Context): Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    fun authenticate(
        context: Context,
        title: String = "Uygulama Kilidi",
        onSuccess: () -> Unit,
        onCancel: () -> Unit = {}
    ) {
        val activity = getActivity(context)
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            onSuccess()
            return
        }

        if (hasCustomPin(activity)) {
            if (isBiometricEnabled(activity) && isBiometricHardwareAvailable(activity) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val handler = MAIN_EXECUTOR.handler
                val cancellationSignal = CancellationSignal()
                var promptDismissed = false

                val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        promptDismissed = true
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (!promptDismissed) {
                            promptDismissed = true
                            showPinUnlockDialog(activity, title, onSuccess, onCancel)
                        }
                    }
                }

                val bp = BiometricPrompt.Builder(activity)
                    .setTitle(title)
                    .setSubtitle("Parmak izinizi okutun veya PIN kullanın")
                    .setNegativeButton("PIN Gir", { runnable: Runnable -> handler.post(runnable) }, { _, _ ->
                        showPinUnlockDialog(activity, title, onSuccess, onCancel)
                    })
                    .build()

                bp.authenticate(cancellationSignal, { runnable: Runnable -> handler.post(runnable) }, authenticationCallback)
            } else {
                showPinUnlockDialog(activity, title, onSuccess, onCancel)
            }
        } else {
            // No custom PIN set, fallback to device credentials
            com.saggitt.omega.util.Config.Companion.showLockScreen(activity, title, Runnable {
                onSuccess()
            })
        }
    }

    fun showPinUnlockDialog(
        activity: Activity,
        title: String = "Uygulama Kilidi",
        onSuccess: () -> Unit,
        onCancel: () -> Unit = {}
    ) {
        if (activity.isFinishing || activity.isDestroyed) return

        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_pin_unlock, null)
        val titleView = view.findViewById<TextView>(R.id.pin_dialog_title)
        val inputView = view.findViewById<EditText>(R.id.pin_dialog_input)
        val errorView = view.findViewById<TextView>(R.id.pin_dialog_error)
        val bioBtn = view.findViewById<Button>(R.id.btn_use_biometric)

        titleView.text = title

        var isUnlocked = false
        var dialog: AlertDialog? = null

        val checkPin = {
            val entered = inputView.text.toString().trim()
            if (verifyPin(activity, entered)) {
                isUnlocked = true
                dialog?.dismiss()
                onSuccess()
            } else {
                errorView.visibility = View.VISIBLE
                inputView.setText("")
            }
        }

        inputView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkPin()
                true
            } else {
                false
            }
        }

        inputView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                errorView.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        if (isBiometricHardwareAvailable(activity) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            bioBtn.visibility = View.VISIBLE
            bioBtn.setOnClickListener {
                dialog?.dismiss()
                authenticate(activity, title, onSuccess, onCancel)
            }
        }

        dialog = AlertDialog.Builder(activity)
            .setView(view)
            .setCancelable(true)
            .setPositiveButton("Aç", null)
            .setNegativeButton("İptal") { d, _ ->
                d.dismiss()
            }
            .setOnDismissListener {
                if (!isUnlocked) {
                    onCancel()
                }
            }
            .create()

        dialog.setOnShowListener {
            val posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            posBtn.setOnClickListener {
                checkPin()
            }
        }

        dialog.show()
    }

    fun showSetPinDialog(context: Context, onPinSet: (Boolean) -> Unit = {}) {
        val activity = getActivity(context) ?: return
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_pin_set, null)
        val pinInput = view.findViewById<EditText>(R.id.set_pin_input)
        val pinConfirm = view.findViewById<EditText>(R.id.set_pin_confirm)
        val errorView = view.findViewById<TextView>(R.id.set_pin_error)

        var dialog: AlertDialog? = null

        val savePin = {
            val pin = pinInput.text.toString().trim()
            val confirm = pinConfirm.text.toString().trim()

            if (pin.length < 4) {
                errorView.text = "PIN en az 4 haneli olmalıdır!"
                errorView.visibility = View.VISIBLE
            } else if (pin != confirm) {
                errorView.text = "PIN kodları eşleşmiyor!"
                errorView.visibility = View.VISIBLE
            } else {
                setCustomPin(context, pin)
                Toast.makeText(context, "Özel PIN başarıyla kaydedildi!", Toast.LENGTH_SHORT).show()
                dialog?.dismiss()
                onPinSet(true)
            }
        }

        pinConfirm.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                savePin()
                true
            } else {
                false
            }
        }

        dialog = AlertDialog.Builder(activity)
            .setView(view)
            .setCancelable(true)
            .setPositiveButton("Kaydet", null)
            .setNegativeButton("İptal") { d, _ ->
                d.dismiss()
                onPinSet(false)
            }
            .create()

        dialog.setOnShowListener {
            val posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            posBtn.setOnClickListener {
                savePin()
            }
        }

        dialog.show()
    }

    fun showChangeOrRemovePinDialog(context: Context, onUpdated: () -> Unit = {}) {
        val activity = getActivity(context) ?: return
        val items = arrayOf("Yeni PIN Belirle", "Özel PIN'i Kaldır (Cihaz Kilidini Kullan)")
        AlertDialog.Builder(activity)
            .setTitle("Özel Kilit Şifresi")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showSetPinDialog(context) { onUpdated() }
                    1 -> {
                        setCustomPin(context, null)
                        Toast.makeText(context, "Özel PIN kaldırıldı. Cihaz kilidi kullanılacak.", Toast.LENGTH_SHORT).show()
                        onUpdated()
                    }
                }
            }
            .setNegativeButton("Kapat", null)
            .show()
    }
}
