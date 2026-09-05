package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.util.AppLanguage
import com.example.util.AppThemeMode

class AppPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("trustpay_prefs", Context.MODE_PRIVATE)

    fun getLanguage(): AppLanguage {
        val code = prefs.getString("selected_language", AppLanguage.ENGLISH.code) ?: AppLanguage.ENGLISH.code
        return AppLanguage.values().firstOrNull { it.code == code } ?: AppLanguage.ENGLISH
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("selected_language", language.code).apply()
    }

    fun getThemeMode(): AppThemeMode {
        val mode = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        return try {
            AppThemeMode.valueOf(mode)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    fun setThemeMode(themeMode: AppThemeMode) {
        prefs.edit().putString("theme_mode", themeMode.name).apply()
    }

    fun isRealSession(): Boolean {
        return prefs.getBoolean("is_real_session", false)
    }

    fun saveUserSession(userId: String, name: String, email: String, role: String, token: String) {
        prefs.edit()
            .putBoolean("is_real_session", true)
            .putString("session_user_id", userId)
            .putString("session_user_name", name)
            .putString("session_user_email", email)
            .putString("session_user_role", role)
            .putString("session_auth_token", token)
            .apply()
    }

    fun clearUserSession() {
        prefs.edit()
            .putBoolean("is_real_session", false)
            .remove("session_user_id")
            .remove("session_user_name")
            .remove("session_user_email")
            .remove("session_user_role")
            .remove("session_auth_token")
            .apply()
    }

    fun getSavedUserId(): String? = prefs.getString("session_user_id", null)
    fun getSavedUserName(): String? = prefs.getString("session_user_name", null)
    fun getSavedUserEmail(): String? = prefs.getString("session_user_email", null)
    fun getSavedUserRole(): String? = prefs.getString("session_user_role", null)
    fun getSavedAuthToken(): String? = prefs.getString("session_auth_token", null)

    fun isBalanceMasked(): Boolean {
        return prefs.getBoolean("is_balance_masked", true)
    }

    fun setBalanceMasked(masked: Boolean) {
        prefs.edit().putBoolean("is_balance_masked", masked).apply()
    }
}

