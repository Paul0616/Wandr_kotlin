package com.encorsa.wandr.models

data class HtmlPageModel (
    val flag: String,
    val htmlPagesDescriptions: List<HtmlPageDescriptionsModel>
)

data class HtmlPageDescriptionsModel(
    val htmlPageID: String,
    val title: String?,
    val html: String?
)

