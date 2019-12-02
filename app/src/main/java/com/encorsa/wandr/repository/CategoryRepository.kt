package com.encorsa.wandr.repository


import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.encorsa.wandr.database.CategoryDatabaseModel
import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.models.Category1RepositoryResult
import com.encorsa.wandr.models.CategoryRepositoryResult
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.utils.DEFAULT_LANGUAGE
import com.encorsa.wandr.utils.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class CategoryRepository(private val app: Application, private val database: WandrDatabaseDao) {

    private val prefs = Prefs(app.applicationContext)
    private val networkError = MutableLiveData<String>()

    fun getCategories(languageTag: String): CategoryRepositoryResult {
        val categories: LiveData<List<CategoryDatabaseModel>> = database.getAllCategoriesForLanguage(languageTag)
        return CategoryRepositoryResult(categories, networkError)
    }

    fun getCurrentCategory(languageTag: String, categoryId: String): Category1RepositoryResult {
        val category: LiveData<CategoryDatabaseModel> = database.getCategoryForLanguage(languageTag, categoryId)
        return Category1RepositoryResult(category, networkError)
    }

    suspend fun refreshCategories() {
        withContext(Dispatchers.IO){
            val options = HashMap<String, String>()
            options.put("languageTag", prefs.currentLanguage ?: DEFAULT_LANGUAGE)
            try {
                val categoriesFromNet =  WandrApi.RETROFIT_SERVICE.getCategories(options).await()
                val rowsInserted = database.insertCategories(categoriesFromNet.asDatabaseModel())
                val expireTime = TimeUnit.MINUTES.toMillis(15)
                val oldCategories = database.getOldDatabaseCategories(System.currentTimeMillis() - expireTime)
                database.deleteOldCategories(oldCategories)

                Log.i("CategoryRepository", "Inserted ${rowsInserted.size} in local database)")
            } catch (e: Exception) {
                networkError.postValue(e.message)
            } catch (ex: HttpException) {
                val error = "CAT ${ex.response().code()} - ${ex.response().errorBody()?.string()}"
                networkError.postValue(error)
            }
        }
    }
}