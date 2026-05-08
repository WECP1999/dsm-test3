package com.example.dsm104_test3.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("aprende_session", Context.MODE_PRIVATE)

    companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_USERNAME = "username"
        const val KEY_EMAIL = "email"
        const val KEY_ROLE = "role"
        const val KEY_LOGGED_IN = "logged_in"
        const val ROLE_DOCENTE = "docente"
        const val ROLE_ESTUDIANTE = "estudiante"
    }

    fun saveSession(userId: String, username: String, email: String, role: String) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            putString(KEY_ROLE, role)
            putBoolean(KEY_LOGGED_IN, true)
            apply()
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn() = prefs.getBoolean(KEY_LOGGED_IN, false)
    fun getUserId() = prefs.getString(KEY_USER_ID, "") ?: ""
    fun getUsername() = prefs.getString(KEY_USERNAME, "") ?: ""
    fun getEmail() = prefs.getString(KEY_EMAIL, "") ?: ""
    fun getRole() = prefs.getString(KEY_ROLE, ROLE_ESTUDIANTE) ?: ROLE_ESTUDIANTE
    fun isDocente() = getRole() == ROLE_DOCENTE
}
