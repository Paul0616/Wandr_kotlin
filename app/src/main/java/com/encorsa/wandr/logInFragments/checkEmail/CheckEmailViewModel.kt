package com.encorsa.wandr.logInFragments.checkEmail

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.network.CallAndStatus
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.network.WandrApiRequestId
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.network.models.LoginRequestModel
import com.encorsa.wandr.network.models.LoginResponseModel
import com.encorsa.wandr.network.models.SecurityCode
import com.encorsa.wandr.utils.DEFAULT_LANGUAGE
import com.encorsa.wandr.utils.Prefs
import kotlinx.coroutines.*
import retrofit2.HttpException

class CheckEmailViewModel(app: Application, val database: WandrDatabaseDao) :
    AndroidViewModel(app) {

    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + viewModelJob)
    private val prefs = Prefs(app.applicationContext)
    val securityCode = MutableLiveData<String?>()
    val currentLanguage = MutableLiveData<String>()


    init {
        Log.i("CheckEmailViewModel", "CREATED")
        currentLanguage.value = prefs.currentLanguage.let {
            it ?: DEFAULT_LANGUAGE
        }
        getLabelByTagAndLanguage("check_email_screen_label", currentLanguage.value!!)
        getLabelByTagAndLanguage("resend_email_button", currentLanguage.value!!)
        getLabelByTagAndLanguage("error_wrong_security_code", currentLanguage.value!!)
        getLabelByTagAndLanguage("security_code_hint", currentLanguage.value!!)
        getLabelByTagAndLanguage("error_email_no_change", currentLanguage.value!!)
        getLabelByTagAndLanguage("check_email_title", currentLanguage.value!!)
    }



    val newEmail = MutableLiveData<String>()

    private val _checkEmailScreenLabel = MutableLiveData<String>()
    val checkEmailScreenLabel: LiveData<String>
        get() = _checkEmailScreenLabel

    private val _resendEmailText = MutableLiveData<String>()
    val resendEmailText: LiveData<String>
        get() = _resendEmailText

    private val _wrongSecurityCode = MutableLiveData<String>()
    val wrongSecurityCode: LiveData<String>
        get() = _wrongSecurityCode

    private val _securityCodeHint = MutableLiveData<String>()
    val securityCodeHint: LiveData<String>
        get() = _securityCodeHint

    private val _checkEmailTitle = MutableLiveData<String>()
    val checkEmailTitle: LiveData<String>
        get() = _checkEmailTitle

    private val _errorEmailNoChange = MutableLiveData<String>()
    val errorEmailNoChange: LiveData<String>
        get() = _errorEmailNoChange

    private val _emailMustBeEdited = MutableLiveData<Boolean>()
    val emailMustBeEdited: LiveData<Boolean>
        get() = _emailMustBeEdited

    private val _emailValidation = MutableLiveData<String>()
    val emailValidation: LiveData<String>
        get() = _emailValidation

    private val _validateSecurityCode = MutableLiveData<Int>()
    val validateSecurityCode: LiveData<Int>
        get() = _validateSecurityCode

    private val _tokenModel = MutableLiveData<LoginResponseModel>()
    val tokenModel: LiveData<LoginResponseModel>
        get() = _tokenModel

    private val _newSecurityCode = MutableLiveData<SecurityCode>()
    val newSecurityCode: LiveData<SecurityCode>
        get() = _newSecurityCode

    private val _error = MutableLiveData<HashMap<String, Any?>>()
    val error: LiveData<HashMap<String, Any?>>
        get() = _error

    private val _status = MutableLiveData<CallAndStatus>()
    val status: LiveData<CallAndStatus>
        get() = _status

    override fun onCleared() {
        super.onCleared()
        Log.i("CheckEmailViewModel", "DESTROYED")
        viewModelJob.cancel()
    }

    fun initiateEditEmail() {
        _emailMustBeEdited.value = !(_emailMustBeEdited.value ?: false)
    }
    fun preserveEditEmailState(){
        _emailMustBeEdited.value = true
    }

    fun onClickContinue() {
        _validateSecurityCode.value = (securityCode.value ?: "0").toInt()
    }


    fun login(credentials: LoginRequestModel) {
        viewModelScope.launch {
            // Get the Deferred object for our Retrofit request
            var getTokenModel = WandrApi.RETROFIT_SERVICE.login(credentials, true)

            // Await the completion of our Retrofit request
            try {
                _status.value = CallAndStatus(WandrApiStatus.LOADING, WandrApiRequestId.LOGIN)
                _tokenModel.value = getTokenModel.await()
                _status.value = CallAndStatus(WandrApiStatus.DONE, WandrApiRequestId.LOGIN)
            } catch (ex: HttpException) {
                _status.value = CallAndStatus(WandrApiStatus.ERROR, WandrApiRequestId.LOGIN)
                val errorMap = HashMap<String, Any?>()
                errorMap.put("code", ex.response().code())
                errorMap.put("message", ex.response().errorBody()?.string())
                _error.value = errorMap

            }
        }
    }

    fun onClickResendEmail() {
        val email = prefs.userEmail
        val firebaseToken = prefs.firebaseToken
        val code: SecurityCode = SecurityCode("0000", firebaseToken!!)
        viewModelScope.launch {
            // Get the Deferred object for our Retrofit request
            var deferredNewSecurityCode = WandrApi.RETROFIT_SERVICE.getNewSecurityCode(email!!, code)

            // Await the completion of our Retrofit request
            try {
                _status.value =
                    CallAndStatus(WandrApiStatus.LOADING, WandrApiRequestId.GET_SECURITY_CODE)
                _newSecurityCode.value = deferredNewSecurityCode.await()
                _status.value =
                    CallAndStatus(WandrApiStatus.DONE, WandrApiRequestId.GET_SECURITY_CODE)
            } catch (ex: HttpException) {
                _status.value =
                    CallAndStatus(WandrApiStatus.ERROR, WandrApiRequestId.GET_SECURITY_CODE)
                val errorMap = HashMap<String, Any?>()
                errorMap.put("code", ex.response().code())
                errorMap.put("message", ex.response().errorBody()?.string())
                _error.value = errorMap

            }
        }
    }

    fun onEditEmailDone(oldEmail: String, newEmail: String) {
        if (!oldEmail.equals(newEmail)) {
            viewModelScope.launch {
                // Get the Deferred object for our Retrofit request
                var deferredUpdateEmail = WandrApi.RETROFIT_SERVICE.updateEmail(oldEmail, newEmail)

                // Await the completion of our Retrofit request
                try {
                    _status.value =
                        CallAndStatus(WandrApiStatus.LOADING, WandrApiRequestId.UPDATE_EMAIL)

                    val response = deferredUpdateEmail.await()
                    val url = response.raw().request().url().toString()
                    _status.value =
                        CallAndStatus(WandrApiStatus.DONE, WandrApiRequestId.UPDATE_EMAIL)
                } catch (ex: HttpException) {
                    _status.value =
                        CallAndStatus(WandrApiStatus.ERROR, WandrApiRequestId.UPDATE_EMAIL)
                    val errorMap = HashMap<String, Any?>()
                    errorMap.put("code", ex.response().code())
                    errorMap.put("message", ex.response().errorBody()?.string())
                    _error.value = errorMap

                }
            }
        }
    }

    fun getLabelByTagAndLanguage(labelTag: String, languageTag: String) {
        ioScope.launch {
            val label = database.findlabelByTag(labelTag, languageTag)
            withContext(Dispatchers.Main) {
                when(labelTag){
                    "check_email_screen_label" -> _checkEmailScreenLabel.value = label?.name
                    "resend_email_button" -> _resendEmailText.value = label?.name
                    "error_wrong_security_code" -> _wrongSecurityCode.value = label?.name
                    "security_code_hint" -> _securityCodeHint.value = label?.name
                    "error_email_no_change" -> _errorEmailNoChange.value = label?.name
                    "check_email_title" -> _checkEmailTitle.value = label?.name
                }
            }
        }
    }
}
