package com.encorsa.wandr.network.models

import com.squareup.moshi.Json

data class LoginResponseModel (
    val token: String,
    val tokenExpirationDate: String,
    val email: String,
    val userName: String,
    val firstName: String,
    val lastName: String,
    @Json(name = "id")
    val userId: String
)
