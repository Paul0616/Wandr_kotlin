package com.encorsa.wandr.network.models

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

    fun asDatabaseModel(): List<ObjectiveDatabaseModel>{
        return objectives.map {
            val isFavorite = it.favorites.size
            ObjectiveDatabaseModel(
                id = it.id,
                subcategoryId = it.subcategoryId,
                categoryId = it.categoryId,
                languageTag = it.objectiveDescriptions.get(0).languageValue.toUpperCase(),
                name = it.objectiveDescriptions.get(0).name,
                address = it.objectiveDescriptions.get(0).address,
                longDescription = it.objectiveDescriptions.get(0).longDescription,
                isFavorite = (it.favorites.size > 0),
                latitude = it.latitude,
                longitude = it.longitude,
                url = it.url,
                email = it.email,
                phoneNumber = it.phoneNumber
            )
        }
    }
}