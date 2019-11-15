package com.encorsa.wandr.models

import android.os.Parcelable
import com.encorsa.wandr.database.MediaDatabaseModel
import kotlinx.android.parcel.Parcelize

data class MediaModel(
    val id: String,
    val url: String?,
    val mediaType: String,
    val isDefault: Boolean,
    val title: String?,
    val objectiveId: String
)