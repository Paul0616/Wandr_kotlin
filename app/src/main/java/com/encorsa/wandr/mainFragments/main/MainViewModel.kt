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
    val currentLanguage = MutableLiveData<String>()
    private val objectiveRepository = ObjectivesRepository(app, database)

    init {
        Log.i("MainViewModel", "CREATED")
        currentLanguage.value = prefs.currentLanguage.let {
            it ?: DEFAULT_LANGUAGE
        }
//        val options = HashMap<String, Any>()
//        options.put("languageTag", currentLanguage.value!!)
//        options.put("pageId", 1)
//        options.put("pageSize", PAGE_SIZE)
//        getObjectives(options, null)
    }

    private val _objectives = MutableLiveData<List<ObjectiveDatabaseModel>>()
    //val objectiveRepositoryResponse: LiveData<ObjectiveRepositoryResult>
    private val objectiveRepositoryResponse: LiveData<ObjectiveRepositoryResult> = Transformations.map(currentLanguage) {
        objectiveRepository.setObjectivesQuery(it, null, null, null, null)
    }

//    val repos: LiveData<List<Repo>> = Transformations.switchMap(repoResult) { it -> it.data }
//    val networkErrors: LiveData<String> = Transformations.switchMap(repoResult) { it ->
//        it.networkErrors
//    }

//    private val _status = MutableLiveData<WandrApiStatus>()
//    val status: LiveData<WandrApiStatus>
//        get() = _status
//
//    private val _error = MutableLiveData<String>()
//    val error: LiveData<String>
//        get() = _error
//
//    private val _objectives = userId
//    val objectives: LiveData<ObjectivePage>
//       get() =_objectives



    fun objectiveListScrolled(visibleItemCount: Int, lastVisibleItemPosition: Int, totalItemCount: Int) {
        if (visibleItemCount + lastVisibleItemPosition + VISIBLE_THRESHOLD >= totalItemCount) {
//            val immutableQuery = lastQueryValue()
//            if (immutableQuery != null) {
//                repository.requestMore(immutableQuery)
//            }
            loadObjectives()
        }
    }

    fun loadObjectives() {
        viewModelScope.launch {
            objectiveRepository.refreshObjectives(currentLanguage.value!!)
        }
    }


//    fun getObjectives(options: HashMap<String, Any>, subcategoryIds: List<String>?) {
//        viewModelScope.launch {
//            // Get the Deferred object for our Retrofit request
//            var getObjectivePageDeferred = WandrApi.RETROFIT_SERVICE.getDObjectives(options, subcategoryIds)
//
//            // Await the completion of our Retrofit request
//            try {
//                _status.value = WandrApiStatus.LOADING
//                val objectivePage = getObjectivePageDeferred.await()
//
//                _status.value = WandrApiStatus.DONE
////                val url = objectivePage.raw().request().url().toString()
////                Log.i("MainViewmodel", url)
//                //objectivesD = objectivePage
//               Log.i("MainViewmodel", objectivePage.raw().request().url().toString())
//            } catch (e: Exception) {
//                _status.value = WandrApiStatus.ERROR
//                _error.value = e.message
//                //_status.value = "Failure: ${e.message}"
//            } catch (ex: HttpException) {
//                _error.value = ex.response().message() + ex.response().errorBody()?.string()
//            }
//        }
//    }

    override fun onCleared() {
        super.onCleared()
        Log.i("MainViewModel", "DESTROYED")
    }
}