package com.encorsa.wandr.models

import com.encorsa.wandr.database.CategoryDatabaseModel

data class CategoryList(
    val items: List<CategoryModel>
){
    fun asDatabaseModel(): List<CategoryDatabaseModel>{
        return items.map {
            CategoryDatabaseModel(
               id = it.id,
                tag = it.tag,
                languageTag = it.categoryNames.single().language,
                name = it.categoryNames.single().name
            )
        }
    }
}

data class CategoryModel(
    val id: String,
    val tag: String,
    val categoryNames: List<Category_SubcategoryNamesModel>
)
