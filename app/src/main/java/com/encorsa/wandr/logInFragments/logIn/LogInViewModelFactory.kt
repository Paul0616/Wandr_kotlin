package com.encorsa.wandr.logInFragments.logIn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.encorsa.wandr.network.models.LoginRequestModel

class LogInViewModelFactory(private val credentials: LoginRequestModel) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LogInViewModel::class.java)) {
                return LogInViewModel(credentials) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
}