package com.encorsa.wandr.logInFragments.viewPrivacy

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.models.HtmlPageModel
import kotlinx.coroutines.*
import com.encorsa.wandr.R
import com.encorsa.wandr.models.LoginRequestModel
import com.encorsa.wandr.models.LoginResponseModel
import com.encorsa.wandr.models.RegistrationRequestModel
import com.encorsa.wandr.network.CallAndStatus
import com.encorsa.wandr.network.WandrApiRequestId
import com.encorsa.wandr.utils.*
import retrofit2.HttpException

class ViewPrivacyViewModel(app: Application, val database: WandrDatabaseDao) :
    AndroidViewModel(app) {


    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    private val prefs = Prefs(app.applicationContext)

    private val _status = MutableLiveData<CallAndStatus>()
    val status: LiveData<CallAndStatus>
        get() = _status

    private val _tokenModel = MutableLiveData<LoginResponseModel>()
    val tokenModel: LiveData<LoginResponseModel>
        get() = _tokenModel


    private val _htmlPage = MutableLiveData<HtmlPageModel>()
    val htmlPage: LiveData<HtmlPageModel>
        get() = _htmlPage

    private val _error = MutableLiveData<HashMap<String, Any?>>()
    val error: LiveData<HashMap<String, Any?>>
        get() = _error

    private val _translations = MutableLiveData<TranslationsPrivacy>(
        TranslationsPrivacy(
            app.getString(R.string.accept_privacy),
            app.getString(R.string.title_privacy)
        )
    )
    val translations: LiveData<TranslationsPrivacy>
        get() = _translations

    init {
        Log.i("ViewUrlViewModel", "CREATED")
        //login(credentials)
        _status.value = null
        getPrivacy(DEFAULT_LANGUAGE, URL_PRIVACY)

        val currentLanguage = prefs.currentLanguage.let {
            it ?: DEFAULT_LANGUAGE
        }
        getLabelsByLanguage(currentLanguage)
    }

    fun registerNewGoogleUser(newGoogleUser: RegistrationRequestModel){
        viewModelScope.launch {
            // Get the Deferred object for our Retrofit request
            var deferredRegistration = WandrApi.RETROFIT_SERVICE.register(newGoogleUser)

            // Await the completion of our Retrofit request
            try {
                _status.value = CallAndStatus(WandrApiStatus.LOADING, WandrApiRequestId.REGISTER)
                val security = deferredRegistration.await()
                _status.value = CallAndStatus(WandrApiStatus.DONE, WandrApiRequestId.REGISTER)
            } catch (ex: HttpException) {
                _status.value = CallAndStatus(WandrApiStatus.ERROR, WandrApiRequestId.REGISTER)
                val errorMap = HashMap<String, Any?>()
                errorMap.put("code", ex.response().code())
                errorMap.put("message", ex.response().errorBody()?.string())
                _error.value = errorMap
            }
        }
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

    private fun getPrivacy(languageTag: String, flag: String?) {
        viewModelScope.launch {
            var options = HashMap<String, String>()
            options.put("languageTag", languageTag)
            if (flag != null)
                options.put("flag", flag)
            var getHtmlPagessDeferred = WandrApi.RETROFIT_SERVICE.getHtmlPages(options)

            // Await the completion of our Retrofit request
            try {
                _status.value = CallAndStatus(WandrApiStatus.LOADING, WandrApiRequestId.GET_PRIVACY)
                val listResult = getHtmlPagessDeferred.await()

                _status.value = CallAndStatus(WandrApiStatus.DONE, WandrApiRequestId.GET_PRIVACY)
                if (!listResult.items.isEmpty())
                    _htmlPage.value = listResult.items.get(0)

            } catch (e: Exception) {
                _status.value = CallAndStatus(WandrApiStatus.ERROR, WandrApiRequestId.GET_PRIVACY)
                val errorMap = HashMap<String, Any?>()
                errorMap.put("message", e.message)
                _error.value = errorMap
                //_status.value = "Failure: ${e.message}"
            } catch (ex: HttpException) {
                val errorMap = HashMap<String, Any?>()
                errorMap.put("code", ex.response().code())
                errorMap.put("message", ex.response().errorBody()?.string())
                _error.value = errorMap
            }
        }
    }

    fun getLabelsByLanguage(languageTag: String) {
        ioScope.launch {

            val ptrivacy = database.findlabelByTag("accept_privacy", languageTag)
            val screenTitle = database.findlabelByTag("terms_and_conditions", languageTag)

            withContext(Dispatchers.Main) {
                _translations.value = TranslationsPrivacy(
                    acceptTerms = ptrivacy?.name,
                    screenTitle = screenTitle?.name
                )
            }

        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("ViewUrlViewModel", "DESTROYED")
        viewModelJob.cancel()
    }
}
