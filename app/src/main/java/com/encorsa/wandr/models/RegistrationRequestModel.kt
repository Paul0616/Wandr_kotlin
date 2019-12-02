package com.encorsa.wandr.models

import android.os.Parcelable
import android.util.Patterns
import kotlinx.android.parcel.Parcelize
import java.util.regex.Pattern
@Parcelize
data class RegistrationRequestModel(
    val email: String,
    val password: String,
    val rePassword: String,
    val firstName: String,
    val lastName: String,
    val firebaseToken: String
): Parcelable {

    val isEmailValid: Boolean
        get() = Patterns.EMAIL_ADDRESS.matcher(email).matches()


    fun isPasswordValid(): Boolean {
        // (?=.*\d)            #   must contains one digit bind 0-9
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