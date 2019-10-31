package com.encorsa.wandr.logInFragments.checkEmail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CheckEmailViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    init {
        Log.i("CheckEmailViewModel", "CREATED")
    }

    private val _emailMustBeEdited = MutableLiveData<Boolean>()
    val  emailMustBeEdited: LiveData<Boolean>
        get() = _emailMustBeEdited

    override fun onCleared() {
        super.onCleared()
        Log.i("CheckEmailViewModel", "DESTROYED")
    }

    fun initiateEditEmail(){
        if (_emailMustBeEdited.value != null)
            _emailMustBeEdited.value = !_emailMustBeEdited.value!!
        else {
            _emailMustBeEdited.value = true
        }
    }
}
