package com.encorsa.wandr.mainFragments.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.*
import com.encorsa.wandr.database.CategoryDatabaseModel
import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.models.CategoryModel
import com.encorsa.wandr.models.CategoryRepositoryResult
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.repository.CategoryRepository
import com.encorsa.wandr.utils.DEFAULT_LANGUAGE
import com.encorsa.wandr.utils.Prefs
import kotlinx.coroutines.launch
import retrofit2.HttpException

class DrawerViewModel(app: Application, val database: WandrDatabaseDao) :
    AndroidViewModel(app) {

    private val prefs = Prefs(app.applicationContext)
    private val categoryRepository = CategoryRepository(app, database)
    val currentLanguage = MutableLiveData<String>()


    private val categoryRepositoryResponse: LiveData<CategoryRepositoryResult> =
        Transformations.map(currentLanguage) {
            Log.i("DrawerViewModel", "categories from repository for language: ${currentLanguage.value}")
            categoryRepository.getCategories(it)
        }

    val menuItems: LiveData<List<CategoryDatabaseModel>> =
        Transformations.switchMap(categoryRepositoryResponse) { it ->
            it.categories
        }

    val networkErrors: LiveData<String> =
        Transformations.switchMap(categoryRepositoryResponse) { it ->
            it.networkErrors
        }

    private val _selectedCategory = MutableLiveData<CategoryDatabaseModel>()
    val selectedCategory: LiveData<CategoryDatabaseModel>
        get() = _selectedCategory


    init {
        Log.i("DrawerViewModel", "CREATED")
        currentLanguage.value = prefs.currentLanguage.let {
            it ?: DEFAULT_LANGUAGE
        }
    }

    fun categoryMenuWasClicked(category: CategoryDatabaseModel){
        _selectedCategory.value = category
    }

    fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.refreshCategories()
        }
    }


    override fun onCleared() {
        super.onCleared()
        Log.i("DrawerViewModel", "DESTROYED")
    }

    fun setCurrentLanguage(tag: String){
        currentLanguage.value = tag
    }
}
