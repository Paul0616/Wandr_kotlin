package com.encorsa.wandr.mainFragments.settings

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.encorsa.wandr.database.LanguageDatabaseModel
import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.utils.DEFAULT_LANGUAGE
import com.encorsa.wandr.utils.Prefs
import kotlinx.coroutines.*

class LanguageSettingsViewModel(app: Application,  val database: WandrDatabaseDao) : AndroidViewModel(app) {
    private var viewModelJob = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    private val currentLanguage = MutableLiveData<String>()
    private val prefs = Prefs(getApplication())

    private val _settingsTitle = MutableLiveData<String>()
    val settingsTitle: LiveData<String>
        get() = _settingsTitle

    private val _listPreferenceTitle = MutableLiveData<String>()
    val listPreferenceTitle: LiveData<String>
        get() = _listPreferenceTitle

    private val _preferenceCategoryTitle = MutableLiveData<String>()
    val preferenceCategoryTitle: LiveData<String>
        get() = _preferenceCategoryTitle

    private val _languagesList = MutableLiveData<List<LanguageDatabaseModel>>()
    val languagesList: LiveData<List<LanguageDatabaseModel>>
        get() = _languagesList

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }


    init {
        Log.i("SettingsViewModel", "CREATED")
        currentLanguage.value = prefs.currentLanguage ?: DEFAULT_LANGUAGE
        getLabelByTagAndLanguage("title_activity_settings", currentLanguage.value!!)
        getLabelByTagAndLanguage("language_title", currentLanguage.value!!)
        getLabelByTagAndLanguage("language_header", currentLanguage.value!!)

        getLanguages()
    }

    private fun getLanguages() {
        ioScope.launch {
            val languages = database.getAllLanguages()
            withContext(Dispatchers.Main) {
                _languagesList.value = languages
            }
        }
    }

    private fun getLabelByTagAndLanguage(labelTag: String, languageTag: String) {
        ioScope.launch {
            val label = database.findlabelByTag(labelTag, languageTag)
            withContext(Dispatchers.Main) {
                when (labelTag) {
                    "title_activity_settings" -> _settingsTitle.value = label?.name
                    "language_title" -> _listPreferenceTitle.value = label?.name
                    "language_header" -> _preferenceCategoryTitle.value = label?.name
                }
            }
        }
    }
}