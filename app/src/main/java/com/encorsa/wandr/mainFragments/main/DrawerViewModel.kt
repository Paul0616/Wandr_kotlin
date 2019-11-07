package com.encorsa.wandr.mainFragments.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.*
import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.models.CategoryModel
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.utils.DEFAULT_LANGUAGE
import com.encorsa.wandr.utils.Prefs
import kotlinx.coroutines.launch
import retrofit2.HttpException

class DrawerViewModel(app: Application, val database: WandrDatabaseDao) :
    AndroidViewModel(app) {

    private val prefs = Prefs(app.applicationContext)

    val currentLanguage = MutableLiveData<String>()
    val menuItems = MutableLiveData<List<CategoryModel>>()//Transformations.switchMap(objectiveRepositoryResponse) { it ->
//        it.objectives
//    }

    val networkErrors =  MutableLiveData<String>() //Transformations.switchMap(objectiveRepositoryResponse) { it ->
//        it.networkErrors
//    }

    init{
        Log.i("DrawerViewModel", "CREATED")
        currentLanguage.value = prefs.currentLanguage.let {
            it ?: DEFAULT_LANGUAGE
        }
        getCategories()
    }

    private fun getCategories() {
        viewModelScope.launch {

            var options = HashMap<String, String>()
            options.put("languageTag", currentLanguage.value!!)
            // Get the Deferred object for our Retrofit request
            var getCategoriesDeferred = WandrApi.RETROFIT_SERVICE.getCategories(options)

            // Await the completion of our Retrofit request
            try {
               // _status.value = WandrApiStatus.LOADING
                val listResult = getCategoriesDeferred.await()
               // _status.value = WandrApiStatus.DONE
                menuItems.value = listResult.items
            }
            catch (e: Exception) {
                //_status.value = WandrApiStatus.ERROR
                networkErrors.value = e.message
                //_status.value = "Failure: ${e.message}"
            }
            catch (ex: HttpException){
                networkErrors.value = ex.response().message() + ex.response().errorBody()?.string()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("DrawerViewModel", "DESTROYED")
    }
}
