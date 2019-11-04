package com.encorsa.wandr.network.models

data class MediaModel(
    val id: String,
    val url: String?,
    val mediaType: String,
    val isDefault: Boolean,
    val title: String?
)