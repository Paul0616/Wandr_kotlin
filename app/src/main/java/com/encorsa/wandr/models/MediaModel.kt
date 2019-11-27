package com.encorsa.wandr.models

import java.net.MalformedURLException
import java.net.URL


data class MediaModel(
    val id: String,
    val url: String?,
    val mediaType: String,
    val isDefault: Boolean,
    val title: String?,
    val objectiveId: String
)