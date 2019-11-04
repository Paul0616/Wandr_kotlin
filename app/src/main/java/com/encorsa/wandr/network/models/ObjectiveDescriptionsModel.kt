package com.encorsa.wandr.network.models

import com.encorsa.wandr.utils.smartTruncate
import com.squareup.moshi.Json

data class ObjectiveDescriptionsModel (
    @Json(name = "language")
    val languageValue: String,
    val name: String?,
    val address: String?,
    val longDescription: String?
){

    val shortDescription: String?
        get() = longDescription?.smartTruncate(200)
}