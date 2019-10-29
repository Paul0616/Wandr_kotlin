package com.encorsa.wandr.utils

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {

    private val PREFS_FILENAME = "com.encorsa.wandr.prefs"
    private val USER_EMAIL = "user_email"
    private val USER_NAME = "user_name"
    private val USER_ID = "user_id"
    private val USER_PASSWORD = "user_password"
    private val TOKEN = "token"
    private val TOKEN_EXPIRE_AT = "token_expire_at"
    private val FIRST_NAME = "first_name"
    private val CURRENT_CATEGORY_ID = "current_category_id"
    private val CURRENT_LANGUAGE = "current_language"

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

    var password: String?
        get() = prefs.getString(USER_PASSWORD, null)
        set(value) = prefs.edit().putString(USER_PASSWORD, value).apply()

    var token: String?
        get() = prefs.getString(TOKEN, null)
        set(value) = prefs.edit().putString(TOKEN, value).apply()

    var firstName: String?
        get() = prefs.getString(FIRST_NAME, null)
        set(value) = prefs.edit().putString(FIRST_NAME, value).apply()

    var currentCategoryId: String?
        get() = prefs.getString(CURRENT_CATEGORY_ID, null)
        set(value) = prefs.edit().putString(CURRENT_CATEGORY_ID, value).apply()

    var tokenExpireAtInMillis: Long
        get() = prefs.getLong(TOKEN_EXPIRE_AT, 0L)
        set(value) = prefs.edit().putLong(TOKEN_EXPIRE_AT, value).apply()

    var currentLanguage: String?
        get() = prefs.getString(CURRENT_LANGUAGE, null)
        set(value) = prefs.edit().putString(CURRENT_LANGUAGE, value).apply()




    fun logOut() {
        prefs.edit().remove(USER_NAME).apply()
        prefs.edit().remove(USER_ID).apply()
        prefs.edit().remove(USER_PASSWORD).apply()
        prefs.edit().remove(TOKEN).apply()
        prefs.edit().remove(TOKEN_EXPIRE_AT).apply()
        prefs.edit().remove(FIRST_NAME).apply()
        prefs.edit().remove(CURRENT_CATEGORY_ID).apply()
    }

}