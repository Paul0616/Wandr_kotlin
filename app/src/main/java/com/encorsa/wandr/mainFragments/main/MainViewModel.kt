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
import com.encorsa.wandr.repository.ObjectivesRepository
import com.encorsa.wandr.utils.DEFAULT_LANGUAGE
import com.encorsa.wandr.utils.Prefs
import com.encorsa.wandr.utils.Utilities
import com.encorsa.wandr.database.SubcategoryDatabaseModel
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.repository.SubcategoryRepository
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.*
import com.encorsa.wandr.models.*
import com.encorsa.wandr.adapters.ViewClicked
import retrofit2.HttpException
import com.encorsa.wandr.R
import com.encorsa.wandr.utils.TranslationsMain


class MainViewModel(app: Application, val database: WandrDatabaseDao) :
    AndroidViewModel(app) {

    companion object {
        private const val VISIBLE_THRESHOLD = 5
    }

    private var viewModelJob = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    //    private val dataSource = WandrDatabase.getInstance(app).wandrDatabaseDao
    private val prefs = Prefs(app.applicationContext)
    private val objectiveRepository = ObjectivesRepository(app, database)
    private val subcategoryRepository = SubcategoryRepository(app, database)
    //private val categoryRepository = CategoryRepository(app, database)
    private val showFavorite = MutableLiveData<Boolean>()
    private val search = MutableLiveData<String>()
    private val queryObjectives = MutableLiveData<SupportSQLiteQuery>()
    private val querySubcategory = MutableLiveData<String>()
    private val categoryId = MutableLiveData<String>()
    private val subcategoryIds = MutableLiveData<Array<String>>()

    private val _currentLanguage = MutableLiveData<String>()
    val currentLanguage: LiveData<String>
        get() = _currentLanguage

    private val _translations = MutableLiveData<TranslationsMain>(
        TranslationsMain(
            app.getString(R.string.no_info),
            app.getString(R.string.more),
            app.getString(R.string.no_results),
            app.getString(R.string.subcategories)
        )
    )
    val translationsMain: LiveData<TranslationsMain>
        get() = _translations

    private val _favoriteId = MutableLiveData<String>()
    val favoriteId: LiveData<String>
        get() = _favoriteId

    //   private val deletedFavorite = MutableLiveData<Boolean>(false)


    private val _subcategoryFilterApplied = MutableLiveData<Boolean>()
    val subcategoryFilterApplied: LiveData<Boolean>
        get() = _subcategoryFilterApplied

    private val _chipsGroupIsVisible = MutableLiveData<Boolean>()
    val chipsGroupIsVisible: LiveData<Boolean>
        get() = _chipsGroupIsVisible

    private val _navigateToDetails = MutableLiveData<ObjectiveDatabaseModel>()
    val navigateToDetails: LiveData<ObjectiveDatabaseModel>
        get() = _navigateToDetails

    private val _navigateToUrl = MutableLiveData<ObjectiveDatabaseModel>()
    val navigateToUrl: LiveData<ObjectiveDatabaseModel>
        get() = _navigateToUrl

    private val _navigateToSettings = MutableLiveData<Boolean>(false)
    val navigateToSettings: LiveData<Boolean>
        get() = _navigateToSettings

    private val _navigateToMap = MutableLiveData<ObjectiveDatabaseModel>()
    val navigateToMap: LiveData<ObjectiveDatabaseModel>
        get() = _navigateToMap

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    private lateinit var queryModel: QueryModel


    init {
        Log.i("MainViewModel", "CREATED")

        _currentLanguage.value = prefs.currentLanguage ?: DEFAULT_LANGUAGE
        _chipsGroupIsVisible.value = false
        _subcategoryFilterApplied.value = false

        Log.i("MainViewModel", "INIT makeQuery was called")
    }


    /*  --------------------------------------------
     *  define receiving media from repository
     *  every time when value of @param queryObjectives changes
     *  - loading from network and storing in repository is called
     *  - objectiveRepositoryResponse will change too
     *  --------------------------------------------
     */
    private val objectivesRepositoryResponse: LiveData<ObjectivesRepositoryResult> =
        Transformations.map(queryObjectives) {
            loadObjectives(true)
            Log.i("MainViewModel", "media from repository conform model: ${queryModel}")
            objectiveRepository.getRepositoryObjectiveWithFilter(it)
        }

    var objectives: LiveData<List<ObjectiveDatabaseModel>> =
        Transformations.switchMap(objectivesRepositoryResponse) { it ->
            it.objectives
        }

    var networkErrors: LiveData<String> =
        Transformations.switchMap(objectivesRepositoryResponse) { it ->
            it.networkErrors
        }

    private fun loadObjectives(filterHasChanged: Boolean) {
        viewModelScope.launch {
            objectiveRepository.makeNetworkCallAndRefreshDatabase(queryModel, filterHasChanged)
        }
    }

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


    /* -----------------------
     *  click on objective row
     * ------------------------
     */
    fun objectiveWasClicked(objective: ObjectiveDatabaseModel, viewClicked: ViewClicked) {
        when (viewClicked) {
            ViewClicked.OBJECTIVE -> _navigateToDetails.value = objective
            ViewClicked.FAVORITE -> favoriteWasClicked(objective, prefs.userId)
            ViewClicked.URL -> urlWasClicked(objective)
            ViewClicked.LOCATION -> locationWasClicked(objective)
        }
    }

    fun displayDetailsComplete() {
        _navigateToDetails.value = null
    }

    fun navigateToSettings() {
        _navigateToSettings.value = true
    }

    fun navigateToSettingsComplete() {
        _navigateToSettings.value = false
    }

    fun navigateToMapComplete() {
        _navigateToMap.value = null
    }

    fun navigateToUrlComplete() {
        _navigateToUrl.value = null
    }

    fun favoriteWasClicked(objective: ObjectiveDatabaseModel, userId: String?) {
        val shouldAddToFavorite = !objective.isFavorite
        Log.i(
            "MainViewModel",
            "ADD TO FAVORITE: ID:${objective.id}"
        )
        shouldAddToFavorite.let {
            if (it) {
                val favoriteForInsert = FavoriteInsertModel(userId!!, objective.id)
                addTofavorite(favoriteForInsert)
            } else {
                deleteFromFavorite(objective.favoriteId)
            }
        }

    }

    fun urlWasClicked(objective: ObjectiveDatabaseModel) {
        Log.i("MainViewModel", "URL clicked")
        _navigateToUrl.value = objective

    }

    fun locationWasClicked(objective: ObjectiveDatabaseModel) {
        Log.i("MainViewModel", "LOCATION clicked")
        _navigateToMap.value = objective
    }

    /* ---------------------------------------------------
    *  first check if token is expired and if is make network login call
    *  make network delete favorite call
    *  capturing errors in error LiveData
    *  if call was succesfull favoriteId will change and in MainFragment call refresh screen
    * ---------------------------------------------------
    */
    private fun deleteFromFavorite(favoriteId: String?) {
        ioScope.launch {
            val time = System.currentTimeMillis()
            var err: String? = null
            var favoriteIdModel: FavoriteIdModel? = null
            try {
                if (prefs.tokenExpireAtInMillis < time) {
                    val credentials = LoginRequestModel(prefs.userEmail!!, prefs.password!!)
                    val getTokenModel = WandrApi.RETROFIT_SERVICE.login(credentials, false)
                    val tokenModel = getTokenModel.await()
                    prefs.userEmail = tokenModel.email
                    prefs.userId = tokenModel.userId
                    prefs.userName = tokenModel.userName
                    prefs.token = tokenModel.token
                    prefs.firstName = tokenModel.firstName
                    val tokenExpireAt = Utilities.getLongDate(tokenModel.tokenExpirationDate)
                    if (null != tokenExpireAt)
                        prefs.tokenExpireAtInMillis = tokenExpireAt
                }
                val token = "Bearer ${prefs.token}"
                val defferedIdModel =
                    WandrApi.RETROFIT_SERVICE.removeFavorite(favoriteId!!, token)
                favoriteIdModel = defferedIdModel.await()
            } catch (e: Exception) {
                err = e.message!!
            } catch (ex: HttpException) {
                err = ex.response().message() + ex.response().errorBody()?.string()
            }
            withContext(Dispatchers.Main) {
                err?.let {
                    _error.value = err
                }

                favoriteIdModel?.let {
                    Log.i("TEST", favoriteIdModel.id)
                    _favoriteId.value = favoriteIdModel.id
                }

            }
        }
    }

    /* ---------------------------------------------------
    *  first check if token is expired and if it is, make network login call
    *  make network insert favorite call
    *  capturing errors in error LiveData
    *  if call was succesfull favoriteId will change and in MainFragment call refresh screen
    * ---------------------------------------------------
    */
    private fun addTofavorite(favorite: FavoriteInsertModel) {
        ioScope.launch {
            val time = System.currentTimeMillis()
            var err: String? = null
            var favoriteIdModel: FavoriteIdModel? = null
            try {
                if (prefs.tokenExpireAtInMillis < time) {
                    val credentials = LoginRequestModel(prefs.userEmail!!, prefs.password!!)
                    val getTokenModel = WandrApi.RETROFIT_SERVICE.login(credentials, false)
                    val tokenModel = getTokenModel.await()
                    prefs.userEmail = tokenModel.email
                    prefs.userId = tokenModel.userId
                    prefs.userName = tokenModel.userName
                    prefs.token = tokenModel.token
                    prefs.firstName = tokenModel.firstName
                    val tokenExpireAt = Utilities.getLongDate(tokenModel.tokenExpirationDate)
                    if (null != tokenExpireAt)
                        prefs.tokenExpireAtInMillis = tokenExpireAt
                }
                val token = "Bearer ${prefs.token}"
                val defferedIdModel =
                    WandrApi.RETROFIT_SERVICE.addFavorite(favorite, token, "application/json")
                favoriteIdModel = defferedIdModel.await()
            } catch (e: Exception) {
                err = e.message!!
            } catch (ex: HttpException) {
                err = ex.response().message() + ex.response().errorBody()?.string()
            }
            withContext(Dispatchers.Main) {
                err?.let {
                    _error.value = err
                }

                favoriteIdModel?.let {
                    Log.i("TEST", favoriteIdModel.id)
                    _favoriteId.value = favoriteIdModel.id
                }

            }
        }
    }

    /* ---------------------------------------------------
     *  refresh repository and screen with current filter
     * ---------------------------------------------------
     */
    fun refreshWithCurrentFilter() {
        loadObjectives(true)
        queryObjectives.value?.let {
            val result = objectiveRepository.getRepositoryObjectiveWithFilter(it)
            objectives = result.objectives
            networkErrors = result.networkErrors
        }
    }

    /*  --------------------------------------------
     *  define receiving subcategories from repository
     *  every time when value of querySubcategory changes
     *  - loading from network and storing in repository is called
     *  - subcategoryRepositoryResponse will change too
     *  --------------------------------------------
     */
    private val subcategoryRepositoryResponse: LiveData<SubcategoryRepositoryResult> =
        Transformations.map(querySubcategory) {
            loadSubcategories()
            Log.i(
                "MainViewModel",
                "subcategories from repository for language: ${prefs.currentLanguage
                    ?: DEFAULT_LANGUAGE} and categoryId: ${it}"
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

    private fun loadSubcategories() {
        viewModelScope.launch {
            subcategoryRepository.refreshCategories()
        }
    }

    fun setCurrentCategory(categoryId: String?) {
        querySubcategory.postValue(categoryId)
    }

    /*  --------------------------
     *   set filters for media
     *  ----------------------------
     */
    fun setSearch(searchText: String?) {
        search.value = searchText
        makeQuery()
        Log.i("MainViewModel", "SET SEARCH makeQuery was called")
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
//        navigateToSettings()
    }

    fun makeQueryForCategoryId(id: String?) {
        categoryId.value = id
        makeQuery()
        Log.i("MainViewModel", "SET CATEGORY makeQuery was called")
    }

    fun setSubcategoryIds(ids: Array<String>?) {
        subcategoryIds.value = ids
        makeQuery()
        Log.i("MainViewModel", "SET SUBCATEGORY makeQuery was called")
    }

    /*  --------------------------
     *   subcategories chips
     *  ----------------------------
     */
    fun changeChipsGroupVisibility() {
        _chipsGroupIsVisible.value = !_chipsGroupIsVisible.value!!
    }

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
        } else {
            setSubcategoryIds(selectedSubcategories.toTypedArray())
            _subcategoryFilterApplied.value = true
        }
    }

    /*  -----------------------------------
    *   LANGUAGE CHANGE:
    *   - get labels for current tag language
    *   - set new language
    *  ------------------------------------
    */
    fun getLabelByTagAndLanguage(languageTag: String) {
        ioScope.launch {
            val noInfo = database.findlabelByTag("no_info", languageTag)
            val more = database.findlabelByTag("more", languageTag)
            val noRecords = database.findlabelByTag("no_records", languageTag)
            val subcategories = database.findlabelByTag("subcategory", languageTag)
            withContext(Dispatchers.Main) {
                _translations.value = TranslationsMain(
                    noInfo = noInfo?.name,
                    more = more?.name,
                    noRecords = noRecords?.name,
                    subcategories = subcategories?.name
                )
                Log.i(
                    "TRANSLATIONS1",
                    "${_translations.value?.noInfo} - ${_translations.value?.more} - ${_translations.value?.noRecords} - ${_translations.value?.subcategories}"
                )
            }
        }
    }

    fun setCurrentLanguage(language: String?) {
        _currentLanguage.value = language
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("MainViewModel", "DESTROYED")
        viewModelJob.cancel()
    }


}