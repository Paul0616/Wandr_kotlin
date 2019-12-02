package com.encorsa.wandr.utils

data class TranslationsRegistration(
    var email: String? = null,
    var password: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var confirmPassword: String? = null,
    var errorInvalidPassword: String? = null,
    var errorPasswordsMatch: String? = null,
    var errorFieldRequired: String? = null,
    var errorInvalidEmail: String? = null,
    var passwordMessage1: String? = null,
    var passwordMessage2: String? = null,
    var passwordMessage3: String? = null,
    var passwordMessage4: String? = null,
    var passwordMessage5: String? = null,
    var screenTitle: String? = null)
