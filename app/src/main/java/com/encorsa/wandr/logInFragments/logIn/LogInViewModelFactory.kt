package com.encorsa.wandr.logInFragments.logIn

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.encorsa.wandr.database.WandrDatabaseDao
import com.google.android.gms.auth.api.signin.GoogleSignInClient

class LogInViewModelFactory(private val application: Application,
                            private val dataSource: WandrDatabaseDao,
                            private val googleSignIn: GoogleSignInClient
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogInViewModel::class.java)){
            return LogInViewModel(application, dataSource, googleSignIn) as T
        }
        throw java.lang.IllegalArgumentException("Unknown ViewModel class")
    }

}