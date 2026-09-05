package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

sealed class PinVerificationResult {
    object Success : PinVerificationResult()
    data class Incorrect(val remainingAttempts: Int) : PinVerificationResult()
    data class LockedOut(val secondsRemaining: Long) : PinVerificationResult()
    object PinNotSet : PinVerificationResult()
}

sealed class PinChangeResult {
    object Success : PinChangeResult()
    data class InvalidOldPin(val remainingAttempts: Int) : PinChangeResult()
    data class LockedOut(val secondsRemaining: Long) : PinChangeResult()
    data class InvalidNewPin(val reason: String) : PinChangeResult()
}

data class PendingPaymentRequest(
    val offlineOption: String,
    val onSuccess: (com.example.data.model.Transaction) -> Unit,
    val onDeclined: (com.example.engine.TrustDecision) -> Unit
)

sealed class PinDialogState {
    object Hidden : PinDialogState()
    data class SetupPin(val pendingRequest: PendingPaymentRequest?) : PinDialogState()
    data class EnterPin(
        val pendingRequest: PendingPaymentRequest,
        val errorMessage: String? = null,
        val lockoutSeconds: Long = 0L
    ) : PinDialogState()
}

/**
 * Robust PIN Security Manager using PBKDF2WithHmacSHA256 (50,000 iterations, 256-bit key).
 * Handles PIN hashing, verification, failed attempt tracking, 30s lockouts, and PIN changes.
 * Never logs or persists plaintext PINs.
 */
class PinSecurityManager(context: Context) {
    companion object {
        private const val PREFS_NAME = "trustpay_pin_prefs"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LOCKOUT_UNTIL = "lockout_until"

        private const val ITERATIONS = 50000
        private const val KEY_LENGTH = 256
        private const val MAX_FAILED_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 30000L
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isPinSet(): Boolean {
        return !prefs.getString(KEY_PIN_HASH, null).isNullOrBlank()
    }

    fun setupPin(pin: String): Boolean {
        if (pin.length !in 4..6 || !pin.all { it.isDigit() }) {
            return false
        }
        val salt = generateSalt()
        val hash = hashPin(pin, salt)

        prefs.edit()
            .putString(KEY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_PIN_HASH, hash)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .apply()

        return true
    }

    fun verifyPin(enteredPin: String): PinVerificationResult {
        if (!isPinSet()) {
            return PinVerificationResult.PinNotSet
        }

        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        val currentTime = System.currentTimeMillis()
        if (currentTime < lockoutUntil) {
            val remainingSeconds = (lockoutUntil - currentTime + 999L) / 1000L
            return PinVerificationResult.LockedOut(remainingSeconds)
        }

        val storedSaltBase64 = prefs.getString(KEY_PIN_SALT, null) ?: return PinVerificationResult.PinNotSet
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return PinVerificationResult.PinNotSet
        val salt = Base64.decode(storedSaltBase64, Base64.NO_WRAP)

        val computedHash = hashPin(enteredPin, salt)
        if (computedHash == storedHash) {
            // Reset failure counter and lockout on successful PIN verification
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_UNTIL, 0L)
                .apply()
            return PinVerificationResult.Success
        } else {
            val failedAttempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                val newLockoutUntil = currentTime + LOCKOUT_DURATION_MS
                prefs.edit()
                    .putInt(KEY_FAILED_ATTEMPTS, 0)
                    .putLong(KEY_LOCKOUT_UNTIL, newLockoutUntil)
                    .apply()
                return PinVerificationResult.LockedOut(LOCKOUT_DURATION_MS / 1000L)
            } else {
                prefs.edit()
                    .putInt(KEY_FAILED_ATTEMPTS, failedAttempts)
                    .apply()
                val remaining = MAX_FAILED_ATTEMPTS - failedAttempts
                return PinVerificationResult.Incorrect(remaining)
            }
        }
    }

    fun changePin(oldPin: String, newPin: String): PinChangeResult {
        when (val result = verifyPin(oldPin)) {
            is PinVerificationResult.Success -> {
                if (newPin.length !in 4..6 || !newPin.all { it.isDigit() }) {
                    return PinChangeResult.InvalidNewPin("PIN must be 4 to 6 digits")
                }
                setupPin(newPin)
                return PinChangeResult.Success
            }
            is PinVerificationResult.Incorrect -> return PinChangeResult.InvalidOldPin(result.remainingAttempts)
            is PinVerificationResult.LockedOut -> return PinChangeResult.LockedOut(result.secondsRemaining)
            is PinVerificationResult.PinNotSet -> {
                setupPin(newPin)
                return PinChangeResult.Success
            }
        }
    }

    fun getLockoutRemainingSeconds(): Long {
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        val currentTime = System.currentTimeMillis()
        return if (currentTime < lockoutUntil) {
            (lockoutUntil - currentTime + 999L) / 1000L
        } else {
            0L
        }
    }

    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }

    private fun hashPin(pin: String, salt: ByteArray): String {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
}
