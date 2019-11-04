package com.encorsa.wandr.repository


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.encorsa.wandr.database.ObjectiveDatabaseModel
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.network.models.ObjectivePage
import com.encorsa.wandr.utils.PAGE_SIZE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class ObjectivesRepository(private val database: WandrDatabase) {
    val objectives: LiveData<List<ObjectiveDatabaseModel>> =
        database.wandrDatabaseDao.getDatabaseObjectives("RO")
    private var isRequestInProgress = false
    private var lastRequestedPage = 1
    private val networkError = MutableLiveData<String>()


    suspend fun refreshObjectives() {
        if (isRequestInProgress) return
        withContext(Dispatchers.IO) {
            val options = HashMap<String, Any>()
            options.put("languageTag", "RO")
            options.put("pageId", lastRequestedPage)
            options.put("pageSize", PAGE_SIZE)
            isRequestInProgress = true
            try {
                //network API call
                val objectivesNetwork: ObjectivePage =
                    WandrApi.RETROFIT_SERVICE.getObjectives(options, null).await()
                //save in database
                database.wandrDatabaseDao.insertObjectives(*objectivesNetwork.asDatabaseModel())

                lastRequestedPage = objectivesNetwork.currentPage()

                if (!objectivesNetwork.isLastPage()){
                    isRequestInProgress = true
                } else {
                    lastRequestedPage++
                    isRequestInProgress = false
                }
            } catch (e: Exception) {
                networkError.postValue(e.message)
                isRequestInProgress = false
            } catch (ex: HttpException) {
                val error = "OBJ ${ex.response().code()} - ${ex.response().errorBody()?.string()}"
                networkError.postValue(error)
                isRequestInProgress = false
            }
        }
    }
}