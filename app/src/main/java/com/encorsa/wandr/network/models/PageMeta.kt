package com.encorsa.wandr.network.models

data class PageMeta(
    val totalCount: Int,
    val totalPages: Int,
    val pageSize: Int,
    val currentPage: Int,
    val hasNextPage: Boolean,
    val hasPreviousPage: Boolean
)