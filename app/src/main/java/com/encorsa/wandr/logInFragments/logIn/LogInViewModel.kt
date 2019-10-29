package com.encorsa.wandr.logInFragments.logIn

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.network.models.LoginRequestModel
import com.encorsa.wandr.network.models.LoginResponseModel
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.utils.DEFAULT_LANGUAGE
import com.encorsa.wandr.utils.Prefs
import com.encorsa.wandr.utils.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.*

class LogInViewModel(app: Application, val database: WandrDatabaseDao) : AndroidViewModel(app) {

//    // Create a Coroutine scope using a job to be able to cancel when needed
//    private var viewModelJob = Job()
//
//    // the Coroutine runs using the Main (UI) dispatcher
//    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val prefs = Prefs(app.applicationContext)
    //API status of the most recent request
    private val _status = MutableLiveData<WandrApiStatus>()
    val status: LiveData<WandrApiStatus>
        get() = _status

    private val _currentlanguage = MutableLiveData<String>()
    val currentlanguage: LiveData<String>
        get() = _currentlanguage

    private val _tokenModel = MutableLiveData<LoginResponseModel>()
    val tokenModel: LiveData<LoginResponseModel>
        get() = _tokenModel

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    var email = MutableLiveData<String>()
    var password = MutableLiveData<String>()

    private val _userValidation = MutableLiveData<LoginRequestModel>()
    val userValidation: LiveData<LoginRequestModel>
        get() = _userValidation

    init {
        Log.i("LogInViewModel", "CREATED")
        Utilities.setLanguageConfig(app.applicationContext)

        _currentlanguage.value = prefs.currentLanguage.let {
            it ?: DEFAULT_LANGUAGE
        }

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

    fun getLabelByTagAndLanguage(labelTag: String, languageTag: String): String? {
        val label = database.findlabelByTag(labelTag, languageTag)
        return label?.name
    }
}