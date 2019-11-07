package com.encorsa.wandr.models

data class CategoryList(
    val items: List<CategoryModel>
)

data class CategoryModel(
    val id: String,
    val tag: String,
    val categoryNames: List<Category_SubcategoryNamesModel>
)
