package com.encorsa.wandr.models

import androidx.lifecycle.LiveData
import com.encorsa.wandr.database.CategoryDatabaseModel

data class Category1RepositoryResult(
    val categories: LiveData<CategoryDatabaseModel>,
    val networkErrors: LiveData<String>
)