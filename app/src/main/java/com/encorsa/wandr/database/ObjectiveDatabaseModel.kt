package com.encorsa.wandr.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize


@Entity(tableName = "objectives_table")
data class ObjectiveDatabaseModel (
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "subcategoryId")
    val subcategoryId: String,

    @ColumnInfo(name = "categoryId")
    val categoryId: String?,

    @ColumnInfo(name = "languageTag")
    val languageTag: String,

    @ColumnInfo(name = "name")
    val name: String? = null,

    @ColumnInfo(name = "address")
    val address: String? = null,

    @ColumnInfo(name = "longDescription")
    val longDescription: String? = null,

    @ColumnInfo(name = "isFavorite")
    val isFavorite: Boolean,

    @ColumnInfo(name = "favoriteId")
    val favoriteId: String? = null,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "url")
    val url: String? = null,

    @ColumnInfo(name = "email")
    val email: String? = null,

    @ColumnInfo(name = "phoneNumber")
    val phoneNumber: String? = null,

    @ColumnInfo(name = "defaultImageUrl")
    val defaultImageUrl: String? = null,

    var subcategoryName: String = ""
)