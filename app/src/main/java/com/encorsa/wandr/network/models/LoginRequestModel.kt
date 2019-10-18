package com.encorsa.wandr.network.models

import android.util.Patterns

data class LoginRequestModel (
    val email: String,
    val password: String,
    val rememberMe: Boolean = false
) {
    val isEmailValid: Boolean
        get() = Patterns.EMAIL_ADDRESS.matcher(email).matches()


    val isPasswordLengthGreaterThan4: Boolean
        get() = password.length > 4
}