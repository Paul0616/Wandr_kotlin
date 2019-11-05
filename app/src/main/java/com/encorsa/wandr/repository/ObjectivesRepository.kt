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
import com.encorsa.wandr.network.models.QueryModel
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

    fun setObjectivesQuery(query: SupportSQLiteQuery, queryModel: QueryModel): ObjectiveRepositoryResult{
        lastRequestedPage = 1

        val objectives: LiveData<List<ObjectiveDatabaseModel>> =
            database.getDatabaseObjectivesWithRaw(query)
        return ObjectiveRepositoryResult(objectives, networkError)
    }

    suspend fun refreshObjectives(queryModel: QueryModel) {
        if (isRequestInProgress) return
        Log.i("ObjectiveRepository", "page = ${lastRequestedPage.toString()}")
        withContext(Dispatchers.IO) {
            var subs: List<String>? = null
            val options = HashMap<String, Any>()
            options.put("languageTag", queryModel.languageTag)
            options.put("pageId", lastRequestedPage)
            options.put("pageSize", PAGE_SIZE)
            queryModel.onlyFavorite?.let {
                options.put("onlyFavorites", queryModel.onlyFavorite!!)
            }
            queryModel.categoryId?.let {
                options.put("categoryId", queryModel.categoryId!!)
            }
            queryModel.name?.let {
                options.put("attractionName", queryModel.name!!)
            }
            queryModel.subcategoryIds?.let {
                subs = queryModel.subcategoryIds!!.toList() as List<String>
            }
            prefs.userId.apply {
                options.put("userId", prefs.userId!!)
            }
            isRequestInProgress = true
            try {
                //network API call
                val objectivesNetwork: ObjectivePage =
                    WandrApi.RETROFIT_SERVICE.getObjectives(options, subs).await()
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