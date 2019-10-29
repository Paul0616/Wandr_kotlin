package com.encorsa.wandr.logInFragments.register

import android.util.Log
import android.widget.EditText
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.network.WandrApiStatus

import com.encorsa.wandr.network.models.RegistrationRequestModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import android.view.View.OnFocusChangeListener
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.View
import android.widget.TextView
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.KeyEvent
import com.encorsa.wandr.network.CallAndStatus
import com.encorsa.wandr.network.WandrApiRequestId
import com.encorsa.wandr.network.models.LoginRequestModel
import com.encorsa.wandr.network.models.LoginResponseModel


class RegisterViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private val _status = MutableLiveData<CallAndStatus>()
    val status: LiveData<CallAndStatus>
        get() = _status


    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    private val _tokenModel = MutableLiveData<LoginResponseModel>()
    val tokenModel: LiveData<LoginResponseModel>
        get() = _tokenModel

    var email = MutableLiveData<String>()
    var firstName = MutableLiveData<String>()
    var lastname = MutableLiveData<String>()
    var password = MutableLiveData<String>()
    var repassword = MutableLiveData<String>()

    var focus = MutableLiveData<Boolean>()


    private val _userValidation = MutableLiveData<RegistrationRequestModel>()
    val userValidation: LiveData<RegistrationRequestModel>
        get() = _userValidation

    init {
        Log.i("RegisterViewModel", "CREATED")
        //login(credentials)
        _status.value = null
        email.value = ""
        password.value = ""
        repassword.value = ""
        firstName.value = ""
        lastname.value = ""
    }


    fun passwordMatch(): Boolean{
        return password.value.equals(repassword.value)
    }

    fun onClickRegister() {
        val registerUser = RegistrationRequestModel(
            email.value!!,
            password.value!!,
            repassword.value!!,
            firstName.value!!,
            lastname.value!!
        )
        _userValidation.value = registerUser
    }

    fun register(credentials: RegistrationRequestModel) {
        viewModelScope.launch {
            // Get the Deferred object for our Retrofit request
            var deferredRegistration = WandrApi.RETROFIT_SERVICE.register(credentials)

            // Await the completion of our Retrofit request
            try {
                _status.value = CallAndStatus(WandrApiStatus.LOADING, WandrApiRequestId.REGISTER)
                deferredRegistration.await()
                _status.value = CallAndStatus(WandrApiStatus.DONE, WandrApiRequestId.REGISTER)
            } catch (ex: HttpException) {
                _status.value = CallAndStatus(WandrApiStatus.ERROR, WandrApiRequestId.REGISTER)
                _error.value =
                    " " + ex.response().code() + " " + ex.response().errorBody()?.string()
            }
        }
    }

    fun login(credentials: LoginRequestModel) {
        viewModelScope.launch {
            // Get the Deferred object for our Retrofit request
            var getTokenModel = WandrApi.RETROFIT_SERVICE.login(credentials)

            // Await the completion of our Retrofit request
            try {
                _status.value = CallAndStatus(WandrApiStatus.LOADING, WandrApiRequestId.LOGIN)
                _tokenModel.value = getTokenModel.await()
                _status.value = CallAndStatus(WandrApiStatus.DONE, WandrApiRequestId.LOGIN)
            } catch (ex: HttpException) {
                _status.value = CallAndStatus(WandrApiStatus.ERROR, WandrApiRequestId.LOGIN)
                _error.value =
                    " " + ex.response().code() + " " + ex.response().errorBody()?.string()
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        Log.i("RegisterViewModel", "DESTROYED")
    }


    // @BindingAdapter("requestFocus")
    var onFocusChangeListener: OnFocusChangeListener = object : OnFocusChangeListener {

        override fun onFocusChange(view: View, isFocused: Boolean) {
            //Hide Keyboard
            focus.value = isFocused
        }
    }

//    fun onEditorAction(view: TextView, actionId: Int, event: KeyEvent): Boolean {
//       // return false // if you want the default action of the actionNext or so on
//        return true // if you want to intercept
//    }

}
