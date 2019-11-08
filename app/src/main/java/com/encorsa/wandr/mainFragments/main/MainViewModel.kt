package com.encorsa.wandr.mainFragments.main


import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.encorsa.wandr.database.ObjectiveDatabaseModel

import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.models.ObjectiveRepositoryResult
import com.encorsa.wandr.models.QueryModel
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
    private val categoryId = MutableLiveData<String>()
    val currentLanguage = MutableLiveData<String>()
    lateinit var queryModel: QueryModel

    init {
        Log.i("MainViewModel", "CREATED")
        currentLanguage.value = prefs.currentLanguage.let {
            it ?: DEFAULT_LANGUAGE
        }
        makeQuery()
    }


    private val objectiveRepositoryResponse: LiveData<ObjectiveRepositoryResult> =
        Transformations.map(query) {
            loadObjectives(true)
            Log.i("MainViewModel", "objectives from repository conform model: ${queryModel}")
            objectiveRepository.setObjectivesQuery(it, queryModel)
        }

    val objectives: LiveData<List<ObjectiveDatabaseModel>> =
        Transformations.switchMap(objectiveRepositoryResponse) { it ->
            it.objectives
        }

    val networkErrors: LiveData<String> =
        Transformations.switchMap(objectiveRepositoryResponse) { it ->
            it.networkErrors
        }

    private val _selectedObjectiveModel = MutableLiveData<ObjectiveDatabaseModel>()
    val selectedObjectiveModel: LiveData<ObjectiveDatabaseModel>
        get() = _selectedObjectiveModel

    private fun makeQuery() {
        queryModel = QueryModel(currentLanguage.value!!, showFavorite.value, search.value, categoryId.value, null)
        Log.i("MainViewModel", "in query was posted ${queryModel.toString()}")
        query.postValue(Utilities.getQuery(queryModel))
    }

    fun objectiveListScrolled(
        visibleItemCount: Int,
        lastVisibleItemPosition: Int,
        totalItemCount: Int
    ) {
        if (visibleItemCount + lastVisibleItemPosition + VISIBLE_THRESHOLD >= totalItemCount) {
            loadObjectives(false)
        }
    }

    fun objectiveWasClicked(objective: ObjectiveDatabaseModel){
        _selectedObjectiveModel.value = objective
    }

    fun loadObjectives(filterHasChanged: Boolean) {
        viewModelScope.launch {
            objectiveRepository.refreshObjectives(queryModel, filterHasChanged)
        }
    }

    fun setSearch(searchText: String?) {
        search.value = searchText
        makeQuery()
    }

    fun setShowFavorite(view: View) {
        //if was null then true
        //if is true than null
        if (showFavorite.value == null) {
            (view as Button).backgroundTintList =
                ContextCompat.getColorStateList(view.context, R.color.colorAccentLight)
            showFavorite.value = true
        } else {
            (view as Button).backgroundTintList =
                ContextCompat.getColorStateList(view.context, R.color.colorLightGray)
            showFavorite.value = null
        }
        makeQuery()
    }

    fun setCategoryId(id: String?){
        categoryId.value = id
        makeQuery()
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("MainViewModel", "DESTROYED")
    }
}