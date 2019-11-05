package com.encorsa.wandr.mainFragments.main


import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.encorsa.wandr.database.ObjectiveDatabaseModel

import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.network.models.ObjectivePage
import com.encorsa.wandr.network.models.ObjectiveRepositoryResult
import com.encorsa.wandr.repository.ObjectivesRepository
import com.encorsa.wandr.utils.DEFAULT_LANGUAGE
import com.encorsa.wandr.utils.Prefs
import kotlinx.coroutines.launch
import retrofit2.HttpException


class MainViewModel(app: Application, val database: WandrDatabaseDao) :
    AndroidViewModel(app) {

    companion object {
        private const val VISIBLE_THRESHOLD = 5
    }

    private val prefs = Prefs(app.applicationContext)
    private val objectiveRepository = ObjectivesRepository(app, database)

    val currentLanguage = MutableLiveData<String>()
    val showFavorite = MutableLiveData<Boolean>()
    init {
        Log.i("MainViewModel", "CREATED")
        currentLanguage.value = prefs.currentLanguage.let {
            it ?: DEFAULT_LANGUAGE
        }
    }

    private val objectiveRepositoryResponse: LiveData<ObjectiveRepositoryResult> = Transformations.map(currentLanguage) {
        objectiveRepository.setObjectivesQuery(it, showFavorite.value, null, null, null)
    }

    val objectives: LiveData<List<ObjectiveDatabaseModel>> = Transformations.switchMap(objectiveRepositoryResponse) { it ->
        it.objectives
    }

    val networkErrors: LiveData<String> = Transformations.switchMap(objectiveRepositoryResponse) { it ->
        it.networkErrors
    }


    fun objectiveListScrolled(visibleItemCount: Int, lastVisibleItemPosition: Int, totalItemCount: Int) {
        if (visibleItemCount + lastVisibleItemPosition + VISIBLE_THRESHOLD >= totalItemCount) {
            loadObjectives()
        }
    }

    fun loadObjectives() {
        viewModelScope.launch {
            objectiveRepository.refreshObjectives(currentLanguage.value!!)
        }
    }

    fun setShowFavorite(){
        showFavorite.value = !(showFavorite.value ?: false)
        objectiveRepository.setObjectivesQuery(currentLanguage.value!!, showFavorite.value, null, null, null)
    }


    override fun onCleared() {
        super.onCleared()
        Log.i("MainViewModel", "DESTROYED")
    }
}