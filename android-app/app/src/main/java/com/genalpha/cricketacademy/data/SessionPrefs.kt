package com.genalpha.cricketacademy.data

import android.content.Context

class SessionPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("gen_alpha_manager", Context.MODE_PRIVATE)

    fun loadSavedEmail(): String = prefs.getString(KEY_EMAIL, "").orEmpty()

    fun loadSavedPassword(): String = prefs.getString(KEY_PASSWORD, "").orEmpty()

    fun loadDarkModeEnabled(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)

    fun loadSession(): ManagerSession? {
        val email = prefs.getString(KEY_SESSION_EMAIL, "").orEmpty()
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, "").orEmpty()
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, "").orEmpty()

        if (email.isBlank() || accessToken.isBlank()) {
            return null
        }

        return ManagerSession(
            email = email,
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    fun saveRememberedCredentials(email: String, password: String) {
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    fun saveSession(session: ManagerSession) {
        prefs.edit()
            .putString(KEY_SESSION_EMAIL, session.email)
            .putString(KEY_ACCESS_TOKEN, session.accessToken)
            .putString(KEY_REFRESH_TOKEN, session.refreshToken)
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
        private const val KEY_SESSION_EMAIL = "session_email"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
    }
}
