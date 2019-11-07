package com.encorsa.wandr.mainFragments.main


import android.app.Application
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.SearchView
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
        queryModel = QueryModel(currentLanguage.value!!, showFavorite.value, search.value, null, null)
        makeQuery()
    }


    private val objectiveRepositoryResponse: LiveData<ObjectiveRepositoryResult> = Transformations.map(query) {
        loadObjectives(true)
        Log.i("MainViewModel", "objectives from repository conform model: ${queryModel}")
        objectiveRepository.setObjectivesQuery(it, queryModel)
    }

    val objectives: LiveData<List<ObjectiveDatabaseModel>> = Transformations.switchMap(objectiveRepositoryResponse) { it ->
        it.objectives
    }

    val networkErrors: LiveData<String> = Transformations.switchMap(objectiveRepositoryResponse) { it ->
        it.networkErrors
    }

//    var searchViewTextListener = object : SearchView.OnQueryTextListener{
//        override fun onQueryTextSubmit(query: String?): Boolean {
//            Log.i("MainViewModel", "ON TEXT SUBMIT SEARCH VIEW")
//            search.postValue(query)
//            queryModel = QueryModel(currentLanguage.value!!, showFavorite.value, search.value, null, null)
//            makeQuery()
//            return true
//        }
//
//        override fun onQueryTextChange(newText: String?): Boolean {
//            return true
//        }
//    }

//    var searchViewCloseListener = object : SearchView.OnCloseListener{
//        override fun onClose(): Boolean {
//            Log.i("MainViewModel", "ON CLOSE SEARCH VIEW")
//            return true
//        }
//    }

    private fun makeQuery(){
        Log.i("MainViewModel", "in query was posted ${queryModel.toString()}")
        query.postValue(Utilities.getQuery(queryModel))
    }

    fun objectiveListScrolled(visibleItemCount: Int, lastVisibleItemPosition: Int, totalItemCount: Int) {
        if (visibleItemCount + lastVisibleItemPosition + VISIBLE_THRESHOLD >= totalItemCount) {
            loadObjectives(false)
        }
    }

    fun loadObjectives(filterHasChanged: Boolean) {
        viewModelScope.launch {
            objectiveRepository.refreshObjectives(queryModel, filterHasChanged)
        }
    }

    fun setSearch(searchText: String?){
        search.value = searchText
        queryModel = QueryModel(currentLanguage.value!!, showFavorite.value, search.value, null, null)
        makeQuery()
    }

    fun setShowFavorite(view: View){
        //if was null then true
        //if is true than null
        if (showFavorite.value == null){
            (view as Button).backgroundTintList = ContextCompat.getColorStateList(view.context, R.color.colorAccentLight)
            showFavorite.value = true
        }
        else {
            (view as Button).backgroundTintList =  ContextCompat.getColorStateList(view.context, R.color.colorLightGray)
            showFavorite.value = null
        }
        //showFavorite.value = !(showFavorite.value ?: false)
        queryModel = QueryModel(currentLanguage.value!!, showFavorite.value, search.value, null, null)
        makeQuery()
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("MainViewModel", "DESTROYED")
    }
}