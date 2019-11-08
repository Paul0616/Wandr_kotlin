package com.encorsa.wandr.models

import androidx.lifecycle.Transformations
import com.encorsa.wandr.database.ObjectiveDatabaseModel
import com.squareup.moshi.Json

data class ObjectivePage(
    private val pageMeta: PageMeta,
    @Json(name = "objectiveApiModels")
    val objectives: List<ObjectiveModel>
) {
    fun isLastPage(): Boolean {
        return !pageMeta.hasNextPage
    }

    fun currentPage(): Int {
        return pageMeta.currentPage
    }

    fun asDatabaseModel(): List<ObjectiveDatabaseModel> {
        return objectives.map {
            val isFavorite = it.favorites.size > 0
            var defaultImagrUrl: String? = null
            if (!it.media.isEmpty()) {
                defaultImagrUrl = it.media.filter { it.isDefault }.single().url
            }
            ObjectiveDatabaseModel(
                id = it.id,
                subcategoryId = it.subcategoryId,
                categoryId = it.categoryId,
                languageTag = it.objectiveDescriptions.single().languageValue.toUpperCase(),
                name = it.objectiveDescriptions.single().name,
                address = it.objectiveDescriptions.single().address,
                longDescription = it.objectiveDescriptions.single().longDescription,
                isFavorite = isFavorite,
                latitude = it.latitude,
                longitude = it.longitude,
                url = it.url,
                email = it.email,
                phoneNumber = it.phoneNumber,
                defaultImageUrl = defaultImagrUrl

            )
        }
    }
}