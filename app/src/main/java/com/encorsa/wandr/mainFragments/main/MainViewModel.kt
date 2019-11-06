package com.encorsa.wandr.mainFragments.main


import android.app.Application
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.encorsa.wandr.database.ObjectiveDatabaseModel

import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.network.models.ObjectiveRepositoryResult
import com.encorsa.wandr.network.models.QueryModel
import com.encorsa.wandr.repository.ObjectivesRepository
import com.encorsa.wandr.utils.DEFAULT_LANGUAGE
import com.encorsa.wandr.utils.Prefs
import com.encorsa.wandr.utils.Utilities
import kotlinx.coroutines.launch
import com.encorsa.wandr.R


class MainViewModel(app: Application, val database: WandrDatabaseDao) :
    AndroidViewModel(app) {

    companion object {
        private const val VISIBLE_THRESHOLD = 5
    }

    private val prefs = Prefs(app.applicationContext)
    private val objectiveRepository = ObjectivesRepository(app, database)
    private val showFavorite = MutableLiveData<Boolean>()
    private val search = MutableLiveData<String>()
    private val query = MutableLiveData<SupportSQLiteQuery>()
    val currentLanguage = MutableLiveData<String>()
    var queryModel: QueryModel

    init {
        Log.i("MainViewModel", "CREATED")
        currentLanguage.value = prefs.currentLanguage.let {
            it ?: DEFAULT_LANGUAGE
        }
        queryModel = QueryModel(currentLanguage.value!!, showFavorite.value, null, null, null)
        makeQuery()
    }


    private val objectiveRepositoryResponse: LiveData<ObjectiveRepositoryResult> = Transformations.map(query) {
        loadObjectives()
        objectiveRepository.setObjectivesQuery(it, queryModel)
    }

    val objectives: LiveData<List<ObjectiveDatabaseModel>> = Transformations.switchMap(objectiveRepositoryResponse) { it ->
        it.objectives
    }

    val networkErrors: LiveData<String> = Transformations.switchMap(objectiveRepositoryResponse) { it ->
        it.networkErrors
    }

    private fun makeQuery(){
        query.postValue(Utilities.getQuery(queryModel))
    }

    fun objectiveListScrolled(visibleItemCount: Int, lastVisibleItemPosition: Int, totalItemCount: Int) {
        if (visibleItemCount + lastVisibleItemPosition + VISIBLE_THRESHOLD >= totalItemCount) {
            loadObjectives()
        }
    }

    fun loadObjectives() {
        viewModelScope.launch {
            objectiveRepository.refreshObjectives(queryModel)
        }
    }

    fun setShowFavorite(view: View){

        //if was null then true
        //if is true than null

        if (showFavorite.value == null){
            (view as Button).backgroundTintList = ContextCompat.getColorStateList(view.context, R.color.colorAccentLight)
            showFavorite.value = true
        }
        else {
            (view as Button).backgroundTintList = ContextCompat.getColorStateList(view.context, R.color.colorPrimary)
            showFavorite.value = null
        }
        //showFavorite.value = !(showFavorite.value ?: false)
        queryModel = QueryModel(currentLanguage.value!!, showFavorite.value, null, null, null)
        makeQuery()
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("MainViewModel", "DESTROYED")
    }
}