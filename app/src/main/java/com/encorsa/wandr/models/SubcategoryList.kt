package com.encorsa.wandr.models

import com.encorsa.wandr.database.SubcategoryDatabaseModel

data class SubcategoryList(
    val items: List<SubcategoryModel>
){
    fun asDatabaseModel(): List<SubcategoryDatabaseModel>{
        return items.map {
            SubcategoryDatabaseModel(
                id = it.id,
                categoryId = it.categoryId,
                languageTag = it.subcategoryNames.single().language,
                name = it.subcategoryNames.single().name
            )
        }
    }
}

data class SubcategoryModel(
    val id: String,
    val categoryId: String,
    val subcategoryNames: List<Category_SubcategoryNamesModel>
)
