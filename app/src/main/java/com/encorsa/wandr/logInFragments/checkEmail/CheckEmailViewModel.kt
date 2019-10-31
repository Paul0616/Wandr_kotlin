package com.encorsa.wandr.logInFragments.checkEmail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.network.models.LoginRequestModel
import com.encorsa.wandr.network.models.LoginResponseModel
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CheckEmailViewModel : ViewModel() {


    init {
        Log.i("CheckEmailViewModel", "CREATED")
    }

    val securityCode = MutableLiveData<String?>()

    private val _emailMustBeEdited = MutableLiveData<Boolean>()
    val  emailMustBeEdited: LiveData<Boolean>
        get() = _emailMustBeEdited

    private val _validateSecurityCode = MutableLiveData<Int>()
    val  validateSecurityCode: LiveData<Int>
        get() = _validateSecurityCode

    private val _tokenModel = MutableLiveData<LoginResponseModel>()
    val tokenModel: LiveData<LoginResponseModel>
        get() = _tokenModel

    private val _error = MutableLiveData<HashMap<String, Any?>>()
    val error: LiveData<HashMap<String, Any?>>
        get() = _error

    private val _status = MutableLiveData<WandrApiStatus>()
    val status: LiveData<WandrApiStatus>
        get() = _status

    override fun onCleared() {
        super.onCleared()
        Log.i("CheckEmailViewModel", "DESTROYED")
    }

    fun initiateEditEmail(){
        _emailMustBeEdited.value = !(_emailMustBeEdited.value ?: false)
    }

    fun onClickContinue(){
        _validateSecurityCode.value = (securityCode.value ?: "0").toInt()
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
}
