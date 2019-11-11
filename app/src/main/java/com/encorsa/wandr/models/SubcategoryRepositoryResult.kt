package com.encorsa.wandr.models

import androidx.lifecycle.LiveData
import com.encorsa.wandr.database.SubcategoryDatabaseModel

data class SubcategoryRepositoryResult(
    val subcategories: LiveData<List<SubcategoryDatabaseModel>>,
    val networkErrors: LiveData<String>
)