package com.encorsa.wandr.logInFragments.checkEmail

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.encorsa.wandr.database.WandrDatabaseDao

class CheckEmailModelFactory(private val application: Application,
                             private val dataSource: WandrDatabaseDao) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckEmailViewModel::class.java)){
            return CheckEmailViewModel(application, dataSource) as T
        }
        throw java.lang.IllegalArgumentException("Unknown ViewModel class")
    }

}