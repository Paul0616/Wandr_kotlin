package com.encorsa.wandr.mainFragments.details

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.encorsa.wandr.database.ObjectiveDatabaseModel
import com.encorsa.wandr.database.WandrDatabaseDao

class DetailModelFactory(private val application: Application,
                         private val dataSource: WandrDatabaseDao,
                         private val objective: ObjectiveDatabaseModel
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            return DetailViewModel(application, dataSource, objective) as T
        }
        throw java.lang.IllegalArgumentException("Unknown ViewModel class")
    }
}