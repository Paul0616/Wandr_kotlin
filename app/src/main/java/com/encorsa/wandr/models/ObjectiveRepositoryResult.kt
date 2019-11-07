package com.encorsa.wandr.models

import androidx.lifecycle.LiveData
import com.encorsa.wandr.database.ObjectiveDatabaseModel

data class ObjectiveRepositoryResult(
    val objectives: LiveData<List<ObjectiveDatabaseModel>>,
    val networkErrors: LiveData<String>
)