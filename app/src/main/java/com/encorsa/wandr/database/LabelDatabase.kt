package com.encorsa.wandr.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "labels_table")
data class LabelDatabase  (
    @PrimaryKey(autoGenerate = true)
    var rowId: Long = 0L,
    @ColumnInfo(name = "tag")
    val tag: String,
    @ColumnInfo(name = "name")
    var name: String?,
    @ColumnInfo(name = "languageTag")
    var languageTag: String,
    @ColumnInfo(name = "labelId")
    var labelId: String
)