package com.encorsa.wandr.utils

data class TranslationsCheckEmail(
    var checkEmailLabel: String? = null,
    var resendEmailText: String? = null,
    var errorWrongSecurity: String? = null,
    var securityCodeHint: String? = null,
    var errorEmailNoChange: String? = null,
    var checkEmailScreenTitle: String? = null)
