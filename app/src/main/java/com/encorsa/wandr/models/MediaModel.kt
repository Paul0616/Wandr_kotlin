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
) {
    fun withVideoId(): MediaModel {
        return MediaModel(
            id = id,
            url = getVideoIdFromUrl(url),
            mediaType = mediaType,
            isDefault = isDefault,
            title = title,
            objectiveId = objectiveId
        )
    }

    private fun getVideoIdFromUrl(url: String?): String? {
        if (url == null)
            return url
        return try {
            val query = URL(url).query
            val params: List<String> = query.split("&")
            params.find {
                it.split("=").first() == "v"
            }
        } catch (e: MalformedURLException) {
            null
        }
    }
}