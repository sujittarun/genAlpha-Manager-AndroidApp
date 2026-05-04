package com.genalpha.cricketacademy.data

import android.content.Context

class SessionPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("gen_alpha_manager", Context.MODE_PRIVATE)

    fun loadSavedEmail(): String = prefs.getString(KEY_EMAIL, "").orEmpty()

    fun loadSavedPassword(): String = ""

    fun loadDarkModeEnabled(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)

    fun saveRememberedCredentials(email: String, @Suppress("UNUSED_PARAMETER") password: String) {
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .remove(KEY_PASSWORD)
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_SESSION_EMAIL)
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }

    fun saveDarkModeEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_DARK_MODE, enabled)
            .apply()
    }

    companion object {
        private const val KEY_EMAIL = "last_email"
        private const val KEY_PASSWORD = "last_password"
        // Legacy keys are removed by clearSession(); active sessions are no longer persisted.
        private const val KEY_SESSION_EMAIL = "session_email"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
    }
}
