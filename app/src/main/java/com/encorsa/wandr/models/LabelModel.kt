package com.encorsa.wandr.models

data class LabelModel (
    val id: String,
    val tag: String,
    val labelNames: List<LanguageAndNameModel>
)