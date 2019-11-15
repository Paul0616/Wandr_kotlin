package com.encorsa.wandr.models

import androidx.lifecycle.Transformations
import com.encorsa.wandr.database.MediaDatabaseModel
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
            var favoriteId: String? = null
            if (isFavorite)
                favoriteId = it.favorites.single().id
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
                favoriteId = favoriteId,
                latitude = it.latitude,
                longitude = it.longitude,
                url = it.url,
                email = it.email,
                phoneNumber = it.phoneNumber,
                defaultImageUrl = defaultImagrUrl

            )
        }
    }


    fun asDatabaseMediaModel(): List<MediaDatabaseModel> {
        var medias = ArrayList<MediaDatabaseModel>()
        for (objective in objectives){
            val mediaCluster= objective.media.map {
                    MediaDatabaseModel(
                        mediaId = it.id,
                        objectiveId = it.objectiveId,
                        mediaUrl = it.url,
                        mediaType = it.mediaType,
                        isDefault = it.isDefault,
                        title = it.title
                    )
                }
            medias.addAll(mediaCluster)
        }
        return medias
    }

}