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



    fun asDatabaseModels(): List<ObjectiveDatabaseModel> {
        return objectives.map {
            it.asDatabaseModel()
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