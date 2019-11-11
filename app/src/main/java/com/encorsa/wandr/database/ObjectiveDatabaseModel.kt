package com.encorsa.wandr.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "objectives_table")
data class ObjectiveDatabaseModel (
    @PrimaryKey
    @NonNull
    var id: String,

    @ColumnInfo(name = "subcategoryId")
    val subcategoryId: String,

    @ColumnInfo(name = "categoryId")
    var categoryId: String?,

    @ColumnInfo(name = "languageTag")
    var languageTag: String,

    @ColumnInfo(name = "name")
    var name: String? = null,

    @ColumnInfo(name = "address")
    var address: String? = null,

    @ColumnInfo(name = "longDescription")
    var longDescription: String? = null,

    @ColumnInfo(name = "isFavorite")
    var isFavorite: Boolean,

    @ColumnInfo(name = "latitude")
    var latitude: Double,

    @ColumnInfo(name = "longitude")
    var longitude: Double,

    @ColumnInfo(name = "url")
    var url: String? = null,

    @ColumnInfo(name = "email")
    var email: String? = null,

    @ColumnInfo(name = "phoneNumber")
    var phoneNumber: String? = null,

    @ColumnInfo(name = "defaultImageUrl")
    var defaultImageUrl: String? = null,

    var subcategoryName: String = ""
)