package com.encorsa.wandr.models

import com.encorsa.wandr.database.ObjectiveDatabaseModel

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
){
    fun asDatabaseModel(): ObjectiveDatabaseModel {
        val isFavorite = favorites.size > 0
        var defaultImagrUrl: String? = null
        var favoriteId: String? = null
        if (isFavorite)
            favoriteId = favorites.single().id
        if (!media.isEmpty()) {
            defaultImagrUrl = media.filter { it.isDefault }.single().url
        }
        return  ObjectiveDatabaseModel(
            id = id,
            subcategoryId = subcategoryId,
            categoryId = categoryId,
            languageTag = objectiveDescriptions.single().languageValue.toUpperCase(),
            name = objectiveDescriptions.single().name,
            address = objectiveDescriptions.single().address,
            longDescription = objectiveDescriptions.single().longDescription,
            isFavorite = isFavorite,
            favoriteId = favoriteId,
            latitude = latitude,
            longitude = longitude,
            url = url,
            email = email,
            phoneNumber = phoneNumber,
            defaultImageUrl = defaultImagrUrl
        )

    }
}
