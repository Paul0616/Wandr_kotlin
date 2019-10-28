package com.encorsa.wandr.logInFragments.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.encorsa.wandr.network.WandrApiStatus

class RegisterViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private val _status = MutableLiveData<WandrApiStatus>()
    val status: LiveData<WandrApiStatus>
        get() = _status


    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    var email = MutableLiveData<String>()
    var firstName = MutableLiveData<String>()
    var lastname = MutableLiveData<String>()
    var password = MutableLiveData<String>()
    var repassword = MutableLiveData<String>()

    private val _userValidation = MutableLiveData<LoginRequestModel>()
    val userValidation: LiveData<LoginRequestModel>
        get() = _userValidation

    init {
        Log.i("LogInViewModel", "CREATED")
        //login(credentials)
        _status.value = null
        if (prefs.userEmail != null)
            email.value = prefs.userEmail
        else
            email.value = ""
        password.value = ""

    }

    fun onClickLogIn() {
        val loginUser = LoginRequestModel(email.value!!, password.value!!, false)
        _userValidation.value = loginUser
    }

    fun login(credentials: LoginRequestModel) {
        viewModelScope.launch {
            // Get the Deferred object for our Retrofit request
            var getTokenModel = WandrApi.RETROFIT_SERVICE.login(credentials)

            // Await the completion of our Retrofit request
            try {
                _status.value = WandrApiStatus.LOADING
                _tokenModel.value = getTokenModel.await()
                _status.value = WandrApiStatus.DONE
            } catch (ex: HttpException) {
                _status.value = WandrApiStatus.ERROR
                _error.value =
                    " " + ex.response().code() + " " + ex.response().errorBody()?.string()
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        Log.i("LogInViewModel", "DESTROYED")
    }
}
