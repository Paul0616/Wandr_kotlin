package com.encorsa.wandr.models

import com.encorsa.wandr.database.LanguageDatabaseModel

data class LanguagesList (
    val items: List<LanguageDatabaseModel>
)