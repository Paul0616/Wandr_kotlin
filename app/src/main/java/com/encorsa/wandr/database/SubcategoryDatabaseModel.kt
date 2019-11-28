package com.encorsa.wandr.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.encorsa.wandr.models.Category_SubcategoryNamesModel

@Entity(tableName = "subcategories_table")
data class SubcategoryDatabaseModel(
    @PrimaryKey
    @NonNull
    var id: String,

    @ColumnInfo(name = "categoryId")
    val categoryId: String,

    @ColumnInfo(name = "languageTag")
    var languageTag: String,

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "createdTime")
    val createdTime : Long = System.currentTimeMillis()
)

