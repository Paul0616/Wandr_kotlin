package com.encorsa.wandr.network.models

data class LabelModel (
    val id: String,
    val tag: String,
    val labelNames: List<LanguageAndNameModel>
)