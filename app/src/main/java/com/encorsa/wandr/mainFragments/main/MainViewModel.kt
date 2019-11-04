package com.encorsa.wandr.mainFragments.main


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope

import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.network.models.ObjectivePage
import com.encorsa.wandr.utils.DEFAULT_LANGUAGE
import com.encorsa.wandr.utils.PAGE_SIZE
import com.encorsa.wandr.utils.Prefs
import kotlinx.coroutines.launch
import retrofit2.HttpException


class MainViewModel(app: Application, val database: WandrDatabaseDao) :
    AndroidViewModel(app) {

    private val prefs = Prefs(app.applicationContext)
    val currentLanguage = MutableLiveData<String>()
    init {
        Log.i("MainViewModel", "CREATED")
        currentLanguage.value = prefs.currentLanguage.let {
            it ?: DEFAULT_LANGUAGE
        }
        val options = HashMap<String, Any>()
        options.put("languageTag", currentLanguage.value!!)
        options.put("pageId", 1)
        options.put("pageSize", PAGE_SIZE)
        getObjectives(options, null)
    }

    private val _status = MutableLiveData<WandrApiStatus>()
    val status: LiveData<WandrApiStatus>
        get() = _status

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    private val _objectives = MutableLiveData<ObjectivePage>()
    val objectives: LiveData<ObjectivePage>
       get() =_objectives



    fun getObjectives(options: HashMap<String, Any>, subcategoryIds: List<String>?) {
        viewModelScope.launch {
            // Get the Deferred object for our Retrofit request
            var getObjectivePageDeferred = WandrApi.RETROFIT_SERVICE.getObjectives(options, subcategoryIds)

            // Await the completion of our Retrofit request
            try {
                _status.value = WandrApiStatus.LOADING
                val objectivePage = getObjectivePageDeferred.await()
                _status.value = WandrApiStatus.DONE
//                val url = objectivePage.raw().request().url().toString()
//                Log.i("MainViewmodel", url)
                _objectives.value = objectivePage
               // Log.i("MainViewmodel", objectivePage.toString())
            } catch (e: Exception) {
                _status.value = WandrApiStatus.ERROR
                _error.value = e.message
                //_status.value = "Failure: ${e.message}"
            } catch (ex: HttpException) {
                _error.value = ex.response().message() + ex.response().errorBody()?.string()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("MainViewModel", "DESTROYED")
    }
}