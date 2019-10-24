package com.encorsa.wandr.utils

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val PREFS_FILENAME = "com.encorsa.wandr.prefs"
    private val USER_EMAIL = "user_email"
    private val USER_NAME = "user_name"
    private val USER_ID = "user_id"
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    var userEmail: String?
        get() = prefs.getString(USER_EMAIL, null)
        set(value) = prefs.edit().putString(USER_EMAIL, value).apply()

    var userName: String?
        get() = prefs.getString(USER_NAME, null)
        set(value) = prefs.edit().putString(USER_NAME, value).apply()

    var userId: String?
        get() = prefs.getString(USER_ID, null)
        set(value) = prefs.edit().putString(USER_ID, value).apply()

    fun logOut() {
        prefs.edit().remove(USER_NAME).apply()
        prefs.edit().remove(USER_ID).apply()
    }

}