package com.encorsa.wandr.models

import androidx.lifecycle.LiveData
import com.encorsa.wandr.database.CategoryDatabaseModel

data class CategoryRepositoryResult(
    val categories: LiveData<List<CategoryDatabaseModel>>,
    val networkErrors: LiveData<String>
)