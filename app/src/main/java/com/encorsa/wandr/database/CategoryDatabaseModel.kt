package com.encorsa.wandr.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.encorsa.wandr.models.Category_SubcategoryNamesModel

@Entity(tableName = "categories_table")
data class CategoryDatabaseModel(
    @PrimaryKey
    @NonNull
    var id: String,

    @ColumnInfo(name = "tag")
    val tag: String,

    @ColumnInfo(name = "languageTag")
    var languageTag: String,

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "createdTime")
    val createdTime : Long = System.currentTimeMillis()
)

