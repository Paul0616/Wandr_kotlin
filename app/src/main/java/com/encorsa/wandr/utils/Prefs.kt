package com.encorsa.wandr.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color

class Prefs (context: Context) {
    val PREFS_FILENAME = "com.encorsa.wandr.prefs"
    val USER_NAME = "user_name"
    val USER_ID = "user_id"
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    var userName: String?
        get() = prefs.getString(USER_NAME, "")
        set(value) = prefs.edit().putString(USER_NAME, value).apply()

    var userId: String?
        get() = prefs.getString(USER_ID, "")
        set(value) = prefs.edit().putString(USER_ID, value).apply()

}