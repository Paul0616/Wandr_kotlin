package com.encorsa.wandr.repository


import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.encorsa.wandr.database.SubcategoryDatabaseModel
import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.models.SubcategoryRepositoryResult
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.utils.DEFAULT_LANGUAGE
import com.encorsa.wandr.utils.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class SubcategoryRepository(private val app: Application, private val database: WandrDatabaseDao) {

    private val prefs = Prefs(app.applicationContext)
    private val networkError = MutableLiveData<String>()

    fun getSubcategoryForCategory(languageTag: String, categoryId: String?): SubcategoryRepositoryResult{
        val subcategories: LiveData<List<SubcategoryDatabaseModel>> = database.getAllSubcategoriesForLanguageAndCategory(languageTag, categoryId)
        return SubcategoryRepositoryResult(subcategories, networkError)
    }

    suspend fun refreshCategories() {
        withContext(Dispatchers.IO){
            val options = HashMap<String, String>()
            options.put("languageTag", prefs.currentLanguage ?: DEFAULT_LANGUAGE)
            try {
                val subcategoriesFromNet =  WandrApi.RETROFIT_SERVICE.getSubcategories(options).await()
                val rowsInserted = database.insertSubcategories(subcategoriesFromNet.asDatabaseModel())
                val expireTime = TimeUnit.MINUTES.toMillis(15)
                val oldSubcategories = database.getOldDatabaseSubcategories(System.currentTimeMillis() - expireTime)
                database.deleteOldSubcategories(oldSubcategories)
                Log.i("SubcategoryRepository", "Inserted ${rowsInserted.size} in local database)")
            } catch (e: Exception) {
                networkError.postValue(e.message)
            } catch (ex: HttpException) {
                val error = "CAT ${ex.response().code()} - ${ex.response().errorBody()?.string()}"
                networkError.postValue(error)
            }
        }
    }
}