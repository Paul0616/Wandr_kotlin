package com.encorsa.wandr.network.models

import android.util.Patterns
import java.util.regex.Pattern

data class RegistrationRequestModel(
    val email: String,
    val password: String,
    val rePassword: String,
    val firstName: String,
    val lastName: String
) {

    val isEmailValid: Boolean
        get() = Patterns.EMAIL_ADDRESS.matcher(email).matches()


    fun isPasswordValid(): Boolean {
        // (?=.*\d)            #   must contains one digit from 0-9
        // (?=.*[a-z])         #   must contains one lowercase characters
        // (?=.*[A-Z])         #   must contains one uppercase characters
        // (?=.*[!@#$%^&*_+?]) #   must contains one special symbols in the list "@#$%"
        // .                   #   match anything with previous condition checking
        // {5,20}              #   length at least 5 characters and maximum of 20
        val PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*_+?]).{5,20})"
        val p = Pattern.compile(PASSWORD_PATTERN)
        val m = p.matcher(password)
        return m.find()
    }

}