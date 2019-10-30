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
import kotlinx.coroutines.*
import retrofit2.HttpException

class LogInViewModel(app: Application, val database: WandrDatabaseDao) : AndroidViewModel(app) {

    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
    private val ioScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    private val prefs = Prefs(app.applicationContext)
    //API status of the most recent request
    private val _status = MutableLiveData<WandrApiStatus>()
    val status: LiveData<WandrApiStatus>
        get() = _status

    private val _currentlanguage = MutableLiveData<String>()
    val currentlanguage: LiveData<String>
        get() = _currentlanguage

    private val _emailHint = MutableLiveData<String?>()
    val emailHint: LiveData<String?>
        get() = _emailHint

    private val _passwordHint = MutableLiveData<String?>()
    val passwordHint: LiveData<String?>
        get() = _passwordHint

    private val _registerButtonText = MutableLiveData<String?>()
    val registerButtonText: LiveData<String?>
        get() = _registerButtonText

    private val _invalidCredentials = MutableLiveData<String?>()
    val invalidCredentials: LiveData<String?>
        get() = _invalidCredentials

    private val _validationErrorFieldRequired = MutableLiveData<String?>()
    val validationErrorFieldRequired: LiveData<String?>
        get() = _validationErrorFieldRequired

    private val _validationErrorInvalidEmail = MutableLiveData<String?>()
    val validationErrorInvalidEmail: LiveData<String?>
        get() = _validationErrorInvalidEmail

    private val _tokenModel = MutableLiveData<LoginResponseModel>()
    val tokenModel: LiveData<LoginResponseModel>
        get() = _tokenModel

    private val _error = MutableLiveData<HashMap<String, Any?>>()
    val error: LiveData<HashMap<String, Any?>>
        get() = _error

    private val _showPassword = MutableLiveData<Boolean>()
    val showPassword: LiveData<Boolean>
        get() = _showPassword

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

    fun onClickShowPassword() {
        if(_showPassword.value != null)
            _showPassword.value = !_showPassword.value!!
        else {
            _showPassword.value = true
        }
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
                val errorMap = HashMap<String, Any?>()
                errorMap.put("code", ex.response().code())
                errorMap.put("message", ex.response().errorBody()?.string())
                _error.value = errorMap

            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        Log.i("LogInViewModel", "DESTROYED")
        ioScope.cancel()
    }

    fun getLabelByTagAndLanguage(labelTag: String, languageTag: String) {
        ioScope.launch {
            val label = database.findlabelByTag(labelTag, languageTag)
            withContext(Dispatchers.Main) {
                when(labelTag){
                    "email" -> _emailHint.value = label?.name
                    "password" -> _passwordHint.value = label?.name
                    "action_sign_up_short" -> _registerButtonText.value = label?.name
                    "invalid_credentials" -> _invalidCredentials.value = label?.name
                    "error_field_required" -> _validationErrorFieldRequired.value = label?.name
                    "error_invalid_email" -> _validationErrorInvalidEmail.value = label?.name
                }
            }
        }
    }
}