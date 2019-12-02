package com.encorsa.wandr.logInFragments.checkEmail

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.network.CallAndStatus
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.network.WandrApiRequestId
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.models.LoginRequestModel
import com.encorsa.wandr.models.LoginResponseModel
import com.encorsa.wandr.models.SecurityCode
import com.encorsa.wandr.utils.DEFAULT_LANGUAGE
import com.encorsa.wandr.utils.Prefs
import com.encorsa.wandr.utils.TranslationsCheckEmail
import com.encorsa.wandr.R
import kotlinx.coroutines.*
import retrofit2.HttpException

class CheckEmailViewModel(app: Application, val database: WandrDatabaseDao) :
    AndroidViewModel(app) {

    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + viewModelJob)
    private val prefs = Prefs(app.applicationContext)
    val securityCode = MutableLiveData<String?>()



    init {
        Log.i("CheckEmailViewModel", "CREATED")
        val currentLanguage = prefs.currentLanguage.let {
            it ?: DEFAULT_LANGUAGE
        }
        getLabelsByLanguage(currentLanguage)

    }



    val newEmail = MutableLiveData<String>()

    private val _translations = MutableLiveData<TranslationsCheckEmail>(
        TranslationsCheckEmail(
            app.getString(R.string.check_email_screen_label),
            app.getString(R.string.resend_email_button),
            app.getString(R.string.wrong_security_code),
            app.getString(R.string.security_code_hint),
            app.getString(R.string.error_email_no_change),
            ""
        )
    )
    val translations: LiveData<TranslationsCheckEmail>
        get() = _translations


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

    fun getLabelsByLanguage(languageTag: String) {
        ioScope.launch {
            val checkEmailLabel = database.findlabelByTag("check_email_screen_label", languageTag)
            val resendEmail = database.findlabelByTag("resend_email_button", languageTag)
            val erorWrongSecurity = database.findlabelByTag("error_wrong_security_code", languageTag)
            val securityCode = database.findlabelByTag("security_code_hint", languageTag)
            val errorEmailNoChange = database.findlabelByTag("error_email_no_change", languageTag)
            val checkEmailTitle = database.findlabelByTag("check_email_title", languageTag)

            withContext(Dispatchers.Main) {
                _translations.value = TranslationsCheckEmail(
                   checkEmailLabel = checkEmailLabel?.name,
                    resendEmailText = resendEmail?.name,
                    errorWrongSecurity = erorWrongSecurity?.name,
                    securityCodeHint = securityCode?.name,
                    errorEmailNoChange = errorEmailNoChange?.name,
                    checkEmailScreenTitle = checkEmailTitle?.name
                )
                Log.i(
                    "TRANSLATIONS",
                    "${_translations.value?.checkEmailLabel} - " +
                            "${_translations.value?.resendEmailText} -" +
                            " ${_translations.value?.errorWrongSecurity} -" +
                            " ${_translations.value?.securityCodeHint} -" +
                            " ${_translations.value?.errorEmailNoChange} -" +
                            " ${_translations.value?.checkEmailScreenTitle}"
                )
            }
        }
    }
}
