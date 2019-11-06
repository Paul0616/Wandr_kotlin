package com.encorsa.wandr.network.models

data class QueryModel (
    var languageTag: String,
    var onlyFavorite: Boolean?,
    var name: String?,
    var categoryId: String?,
    var subcategoryIds: Array<Any>?
)