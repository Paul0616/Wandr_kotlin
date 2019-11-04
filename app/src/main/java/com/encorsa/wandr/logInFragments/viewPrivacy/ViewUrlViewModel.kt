package com.encorsa.wandr.logInFragments.viewPrivacy

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.network.models.HtmlPageModel
import com.encorsa.wandr.utils.DEFAULT_LANGUAGE
import com.encorsa.wandr.utils.Prefs
import com.encorsa.wandr.utils.URL_PRIVACY
import kotlinx.coroutines.*
import retrofit2.HttpException

class ViewUrlViewModel(app: Application, val database: WandrDatabaseDao) : AndroidViewModel(app) {


    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    private val prefs = Prefs(app.applicationContext)

    val currentLanguage = MutableLiveData<String>()

    private val _status = MutableLiveData<WandrApiStatus>()
    val status: LiveData<WandrApiStatus>
        get() = _status


    private val _htmlPage = MutableLiveData<HtmlPageModel>()
    val htmlPage: LiveData<HtmlPageModel>
        get() = _htmlPage

    private val _error = MutableLiveData<HashMap<String, Any?>>()
    val error: LiveData<HashMap<String, Any?>>
        get() = _error

    private val _acceptPrivacyMessage = MutableLiveData<String?>()
    val acceptPrivacyMessage: LiveData<String?>
        get() = _acceptPrivacyMessage


    init {
        Log.i("ViewUrlViewModel", "CREATED")
        //login(credentials)
        _status.value = null
        getPrivacy(DEFAULT_LANGUAGE, URL_PRIVACY)

        currentLanguage.value = prefs.currentLanguage.let {
            it ?: DEFAULT_LANGUAGE
        }
        getLabelByTagAndLanguage("accept_privacy", currentLanguage.value!!)
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
                _status.value = WandrApiStatus.LOADING
                val listResult = getHtmlPagessDeferred.await()

                _status.value = WandrApiStatus.DONE
                if (!listResult.items.isEmpty())
                    _htmlPage.value = listResult.items.get(0)

            }
            catch (e: Exception) {
                _status.value = WandrApiStatus.ERROR
                val errorMap = HashMap<String, Any?>()
                errorMap.put("message", e.message)
                _error.value = errorMap
                //_status.value = "Failure: ${e.message}"
            }
            catch (ex: HttpException){
                val errorMap = HashMap<String, Any?>()
                errorMap.put("code", ex.response().code())
                errorMap.put("message", ex.response().errorBody()?.string())
                _error.value = errorMap
            }
        }
    }

    fun getLabelByTagAndLanguage(labelTag: String, languageTag: String) {
        ioScope.launch {
            val label = database.findlabelByTag(labelTag, languageTag)
            withContext(Dispatchers.Main) {
                when(labelTag){
                    "accept_privacy" -> _acceptPrivacyMessage.value = label?.name
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("ViewUrlViewModel", "DESTROYED")
        viewModelJob.cancel()
    }
}
