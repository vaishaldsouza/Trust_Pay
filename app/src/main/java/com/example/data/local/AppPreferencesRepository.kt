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
}
