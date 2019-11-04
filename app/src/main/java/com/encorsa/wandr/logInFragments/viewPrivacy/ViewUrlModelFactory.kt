package com.encorsa.wandr.logInFragments.viewPrivacy

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.encorsa.wandr.database.WandrDatabaseDao

class ViewUrlModelFactory(private val application: Application,
                          private val dataSource: WandrDatabaseDao) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewUrlViewModel::class.java)){
            return ViewUrlViewModel(application, dataSource) as T
        }
        throw java.lang.IllegalArgumentException("Unknown ViewModel class")
    }

}