package com.encorsa.wandr.network.models

import com.squareup.moshi.Json

data class ObjectivePage(
    private val pageMeta: PageMeta,
    @Json(name = "objectiveApiModels")
    val objectives: List<ObjectiveModel>
) {
    fun isLastPage(): Boolean {
        return !pageMeta.hasNextPage
    }

    fun currentPage(): Int {
        return pageMeta.currentPage
    }
}