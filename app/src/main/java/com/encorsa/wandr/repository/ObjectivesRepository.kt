package com.encorsa.wandr.repository


import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.sqlite.db.SupportSQLiteQuery
import com.encorsa.wandr.database.ObjectiveDatabaseModel
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.network.models.ObjectivePage
import com.encorsa.wandr.network.models.ObjectiveRepositoryResult
import com.encorsa.wandr.utils.DEFAULT_LANGUAGE
import com.encorsa.wandr.utils.PAGE_SIZE
import com.encorsa.wandr.utils.Prefs
import com.encorsa.wandr.utils.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class ObjectivesRepository(private val app: Application, private val database: WandrDatabaseDao) {

    private val prefs = Prefs(app.applicationContext)





    private var isRequestInProgress = false
    private var lastRequestedPage = 1
    private val networkError = MutableLiveData<String>()

    fun setObjectivesQuery(languageTag: String,
                           onlyFavs: Boolean?,
                           name: String?,
                           categoryId: String?,
                           subcategoryIds: Array<Any>?): ObjectiveRepositoryResult{
        Log.i("ObjectivesRepository",
            "New query: language:${languageTag} favorite: ${onlyFavs?.toString()} name: ${name?.toString()} categoryId: ${categoryId?.toString()} subcategoryIds ${subcategoryIds?.toString()}")
        val query = Utilities.getQuery(languageTag, onlyFavs, name, categoryId, subcategoryIds)
        val objectives: LiveData<List<ObjectiveDatabaseModel>> =
            database.getDatabaseObjectivesWithRaw(query)
        return ObjectiveRepositoryResult(objectives, networkError)
    }

    suspend fun refreshObjectives(languguageTag: String) {
        if (isRequestInProgress) return
        withContext(Dispatchers.IO) {
            val options = HashMap<String, Any>()
            options.put("languageTag", languguageTag)
            options.put("pageId", lastRequestedPage)
            options.put("pageSize", PAGE_SIZE)
            prefs.userId.apply {
                options.put("userId", prefs.userId!!)
            }
            isRequestInProgress = true
            try {
                //network API call
                val objectivesNetwork: ObjectivePage =
                    WandrApi.RETROFIT_SERVICE.getObjectives(options, null).await()
                //save in database
                val rowsInserted = database.insertObjectives(objectivesNetwork.asDatabaseModel())
                Log.i("ObjectiverRepository", "Inserted ${rowsInserted.size.toString()} in local database")
                lastRequestedPage = objectivesNetwork.currentPage()
                isRequestInProgress = false
                if (!objectivesNetwork.isLastPage())
                    lastRequestedPage++
                else
                    isRequestInProgress = true

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