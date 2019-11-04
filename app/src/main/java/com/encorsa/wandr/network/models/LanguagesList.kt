package com.encorsa.wandr.network.models

import com.encorsa.wandr.database.LanguageDatabaseModel

data class LanguagesList (
    val items: List<LanguageDatabaseModel>
)