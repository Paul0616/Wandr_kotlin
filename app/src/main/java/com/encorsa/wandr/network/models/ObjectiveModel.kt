package com.encorsa.wandr.network.models

data class ObjectiveModel(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val url: String?,
    val email: String,
    val phoneNumber: String,
    val objectiveDescriptions: List<ObjectiveDescriptionsModel>,
    val subcategoryId: String,
    val categoryId: String,
    val favorites: List<FavoriteModel>,
    val media: List<MediaModel>
)
