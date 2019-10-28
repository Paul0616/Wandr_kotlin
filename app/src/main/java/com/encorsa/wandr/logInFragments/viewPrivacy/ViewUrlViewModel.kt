package com.encorsa.wandr.logInFragments.viewPrivacy

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.network.models.HtmlPageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ViewUrlViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
  //  private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    private val _status = MutableLiveData<WandrApiStatus>()
    val status: LiveData<WandrApiStatus>
        get() = _status


    private val _htmlPage = MutableLiveData<HtmlPageModel>()
    val htmlPage: LiveData<HtmlPageModel>
        get() = _htmlPage

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    init {
        Log.i("ViewUrlViewModel", "CREATED")
        //login(credentials)
        _status.value = null
        getPrivacy("RO", "PRIVACY")
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
                _error.value = e.message
                //_status.value = "Failure: ${e.message}"
            }
            catch (ex: HttpException){
                _error.value = ex.response().message() + ex.response().errorBody()?.string()
            }
        }
    }

//    fun onClickCheckBox() {
//
//    }

    override fun onCleared() {
        super.onCleared()
        Log.i("ViewUrlViewModel", "DESTROYED")
    }
}
