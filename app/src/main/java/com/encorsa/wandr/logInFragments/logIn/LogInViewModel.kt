package com.encorsa.wandr.logInFragments.logIn

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.encorsa.wandr.network.models.LoginRequestModel
import com.encorsa.wandr.network.models.LoginResponseModel
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.utils.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LogInViewModel(app: Application) : AndroidViewModel(app) {

    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val prefs = Prefs(app.applicationContext)
    //API status of the most recent request
    private val _status = MutableLiveData<WandrApiStatus>()
    val status: LiveData<WandrApiStatus>
        get() = _status

    private val _tokenModel = MutableLiveData<LoginResponseModel>()
    val tokenModel: LiveData<LoginResponseModel>
        get() = _tokenModel

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    var email = MutableLiveData<String>()
    var password = MutableLiveData<String>()

    private val _userRequest = MutableLiveData<LoginRequestModel>()
    val userRequest: LiveData<LoginRequestModel>
        get() = _userRequest

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
        _userRequest.value = loginUser
    }

    fun login(credentials: LoginRequestModel) {
        coroutineScope.launch {
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