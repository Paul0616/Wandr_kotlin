package com.encorsa.wandr.models

import androidx.lifecycle.LiveData
import com.encorsa.wandr.database.MediaDatabaseModel

data class MediaRepositoryResult(
    val media: LiveData<List<MediaDatabaseModel>>,
    val networkErrors: LiveData<String>
)