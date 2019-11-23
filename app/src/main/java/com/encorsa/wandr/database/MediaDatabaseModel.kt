package com.encorsa.wandr.database


import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "media_table")
data class MediaDatabaseModel (
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
    val title: String? = null
): Parcelable