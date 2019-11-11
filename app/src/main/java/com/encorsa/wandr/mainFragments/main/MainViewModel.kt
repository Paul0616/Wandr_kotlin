package com.encorsa.wandr.mainFragments.main


import android.app.Application
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
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
import com.encorsa.wandr.database.SubcategoryDatabaseModel
import com.encorsa.wandr.models.Category1RepositoryResult
import com.encorsa.wandr.models.SubcategoryRepositoryResult
import com.encorsa.wandr.repository.CategoryRepository
import com.encorsa.wandr.repository.SubcategoryRepository
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class MainViewModel(app: Application, val database: WandrDatabaseDao) :
    AndroidViewModel(app) {

    companion object {
        private const val VISIBLE_THRESHOLD = 5
    }

    private val prefs = Prefs(app.applicationContext)
    private val objectiveRepository = ObjectivesRepository(app, database)
    private val subcategoryRepository = SubcategoryRepository(app, database)
    private val categoryRepository = CategoryRepository(app, database)
    private val showFavorite = MutableLiveData<Boolean>()
    private val search = MutableLiveData<String>()
    private val queryObjectives = MutableLiveData<SupportSQLiteQuery>()
    private val querySubcategory = MutableLiveData<String>()
    private val categoryId = MutableLiveData<String>()
    private val subcategoryIds = MutableLiveData<Array<String>>()
    private val currentLanguage = MutableLiveData<String>()


    private val _subcategoryFilterApplied = MutableLiveData<Boolean>()
    val subcategoryFilterApplied: LiveData<Boolean>
        get() = _subcategoryFilterApplied

    private val _chipsGroupIsVisible = MutableLiveData<Boolean>()
    val chipsGroupIsVisible: LiveData<Boolean>
        get() = _chipsGroupIsVisible

    lateinit var queryModel: QueryModel


    init {
        Log.i("MainViewModel", "CREATED")

        currentLanguage.value = prefs.currentLanguage ?: DEFAULT_LANGUAGE
        _chipsGroupIsVisible.value = false
        _subcategoryFilterApplied.value = false
        //makeQuery()
        Log.i("MainViewModel", "INIT makeQuery was called")
    }



    private val objectiveRepositoryResponse: LiveData<ObjectiveRepositoryResult> =
        Transformations.map(queryObjectives) {
            loadObjectives(true)
            Log.i("MainViewModel", "objectives from repository conform model: ${queryModel}")
            objectiveRepository.setDatabaseObjectivesQuery(it)
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
        queryModel = QueryModel(
            prefs.currentLanguage ?: DEFAULT_LANGUAGE,//currentLanguage.value!!,
            showFavorite.value,
            search.value,
            categoryId.value,
            subcategoryIds.value
        )
        Log.i("MainViewModel", "in queryObjectives was posted ${queryModel.toString()}")
        queryObjectives.postValue(Utilities.getQuery(queryModel))
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

    fun objectiveWasClicked(objective: ObjectiveDatabaseModel) {
        _selectedObjectiveModel.value = objective
    }

    fun loadObjectives(filterHasChanged: Boolean) {
        viewModelScope.launch {
            objectiveRepository.makeNetworkCallAndRefreshDatabase(queryModel, filterHasChanged)
        }
    }


    private val subcategoryRepositoryResponse: LiveData<SubcategoryRepositoryResult> =
        Transformations.map(querySubcategory) {
            loadSubcategories()
            Log.i(
                "MainViewModel",
                "subcategories from repository for language: ${prefs.currentLanguage ?: DEFAULT_LANGUAGE} and categoryId: ${it}"
            )
            subcategoryRepository.getSubcategoryForCategory(
                prefs.currentLanguage ?: DEFAULT_LANGUAGE, //currentLanguage.value!!,
                querySubcategory.value
            )
        }

    val subcategoriesList: LiveData<List<SubcategoryDatabaseModel>> =
        Transformations.switchMap(subcategoryRepositoryResponse) { it ->
            it.subcategories
        }

    val networkErrorsSubcategories: LiveData<String> =
        Transformations.switchMap(subcategoryRepositoryResponse) { it ->
            it.networkErrors
        }

    //private val category1Repositoryresponse: LiveData<Category1RepositoryResult> =


    fun loadSubcategories() {
        viewModelScope.launch {
            subcategoryRepository.refreshCategories()
        }
    }

    fun setSearch(searchText: String?) {
        search.value = searchText
        makeQuery()
        Log.i("MainViewModel", "SET SEARCH makeQuery was called")
    }

    fun changeChipsGroupVisibility(){
        _chipsGroupIsVisible.value = !_chipsGroupIsVisible.value!!
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
        Log.i("MainViewModel", "SHOW FAVORITE makeQuery was called")
    }

    fun makeQueryForCategoryId(id: String?) {
        categoryId.value = id
        makeQuery()
        Log.i("MainViewModel", "SET CATEGORY makeQuery was called")
    }

//    fun setLanguguageTag(tag: String) {
////        currentLanguage.value = tag
////        makeQuery()
////        Log.i("MainViewModel", "SET LANGUAGE makeQuery was called")
////    }

    fun getSelectedChipsTags(forParent: CompoundButton) {
        val gr = forParent.parent as ChipGroup
        var selectedSubcategories = mutableListOf<String>()
        for (index in 0 until gr.childCount) {
            val mychip = gr.getChildAt(index) as Chip
            if (mychip.isChecked)
                selectedSubcategories.add(mychip.tag as String)
        }
        if (selectedSubcategories.size == 0) {
            setSubcategoryIds(null)
            _subcategoryFilterApplied.value = false
            _chipsGroupIsVisible.value = false
        }
        else {
            setSubcategoryIds(selectedSubcategories.toTypedArray())
            _subcategoryFilterApplied.value = true
        }
    }

    fun setSubcategoryIds(ids: Array<String>?) {
        subcategoryIds.value = ids
        makeQuery()
        Log.i("MainViewModel", "SET SUBCATEGORY makeQuery was called")
    }

    fun setCurrentCategory(categoryId: String?) {
        querySubcategory.postValue(categoryId)
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("MainViewModel", "DESTROYED")
    }
}