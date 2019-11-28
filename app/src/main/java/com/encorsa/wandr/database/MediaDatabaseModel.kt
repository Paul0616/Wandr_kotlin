package com.encorsa.wandr.database


import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.net.MalformedURLException
import java.net.URL

@Parcelize
@Entity(tableName = "media_table")
data class MediaDatabaseModel(
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "mediaId")
    val mediaId: String,

    @ColumnInfo(name = "objectiveId")
    val objectiveId: String,

    @ColumnInfo(name = "mediaType")
    val mediaType: String?,

    @ColumnInfo(name = "mediaUrl")
    val mediaUrl: String? = null,

    @ColumnInfo(name = "isDefault")
    val isDefault: Boolean,

    @ColumnInfo(name = "title")
    val title: String? = null,

    @ColumnInfo(name = "createdTime")
    val createdTime : Long = System.currentTimeMillis(),

    var isSelected: Boolean = false
) : Parcelable {
    fun withVideoId(): MediaDatabaseModel {
        return MediaDatabaseModel(
            mediaId = this.mediaId,
            objectiveId = this.objectiveId,
            mediaType = this.mediaType,
            mediaUrl = getVideoIdFromUrl(this.mediaUrl),
            isDefault = this.isDefault,
            isSelected = this.isSelected,
            title = this.title
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
        }?.split("=")?.last()
    } catch (e: MalformedURLException) {
        null
    }
}
}