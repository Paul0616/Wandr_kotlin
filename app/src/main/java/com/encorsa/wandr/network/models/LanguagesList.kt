package com.encorsa.wandr.network.models

import com.encorsa.wandr.database.LanguageDatabase

data class LanguagesList (
    val items: List<LanguageDatabase>
)