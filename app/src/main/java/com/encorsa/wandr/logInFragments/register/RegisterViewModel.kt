package com.encorsa.wandr.logInFragments.register

import android.app.Application
import android.util.Log
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.network.WandrApiStatus

import com.encorsa.wandr.network.models.RegistrationRequestModel
import retrofit2.HttpException
import android.view.View.OnFocusChangeListener
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.*
import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.network.CallAndStatus
import com.encorsa.wandr.network.WandrApiRequestId
import com.encorsa.wandr.network.models.LoginRequestModel
import com.encorsa.wandr.network.models.LoginResponseModel
import com.encorsa.wandr.utils.DEFAULT_LANGUAGE
import com.encorsa.wandr.utils.Prefs
import kotlinx.coroutines.*


class RegisterViewModel(app: Application, val database: WandrDatabaseDao) : AndroidViewModel(app) {

    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
    private val ioScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    private val prefs = Prefs(app.applicationContext)

    private val _status = MutableLiveData<CallAndStatus>()
    val status: LiveData<CallAndStatus>
        get() = _status


    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

//    private val _tokenModel = MutableLiveData<LoginResponseModel>()
//    val tokenModel: LiveData<LoginResponseModel>
//        get() = _tokenModel

    //---------validation form
    private val _emailHint = MutableLiveData<String?>()
    val emailHint: LiveData<String?>
        get() = _emailHint

    private val _passwordHint = MutableLiveData<String?>()
    val passwordHint: LiveData<String?>
        get() = _passwordHint

    private val _confirmPasswordHint = MutableLiveData<String?>()
    val confirmPasswordHint: LiveData<String?>
        get() = _confirmPasswordHint

    private val _firstNameHint = MutableLiveData<String?>()
    val firstNameHint: LiveData<String?>
        get() = _firstNameHint

    private val _lastNameHint = MutableLiveData<String?>()
    val lastNameHint: LiveData<String?>
        get() = _lastNameHint

    private val _validationErrorFieldRequired = MutableLiveData<String?>()
    val validationErrorFieldRequired: LiveData<String?>
        get() = _validationErrorFieldRequired

    private val _validationErrorInvalidEmail = MutableLiveData<String?>()
    val validationErrorInvalidEmail: LiveData<String?>
        get() = _validationErrorInvalidEmail

    private val _validationErrorPasswordMatch = MutableLiveData<String?>()
    val validationErrorPasswordMatch: LiveData<String?>
        get() = _validationErrorPasswordMatch

    private val _validationErrorInvalidPassword = MutableLiveData<String?>()
    val validationErrorInvalidPassword: LiveData<String?>
        get() = _validationErrorInvalidPassword

    //-------

    private val _showPassword1 = MutableLiveData<Boolean>()
    val showPassword1: LiveData<Boolean>
        get() = _showPassword1

    private val _showPassword2 = MutableLiveData<Boolean>()
    val showPassword2: LiveData<Boolean>
        get() = _showPassword2

    val currentLanguage = MutableLiveData<String>()
    //get() = currentLanguage

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
        currentLanguage.value = prefs.currentLanguage.let {
            it ?: DEFAULT_LANGUAGE
        }

        getLabelByTagAndLanguage("email", currentLanguage.value!!)
        getLabelByTagAndLanguage("password", currentLanguage.value!!)
        getLabelByTagAndLanguage("first_name", currentLanguage.value!!)
        getLabelByTagAndLanguage("last_name", currentLanguage.value!!)
        getLabelByTagAndLanguage("confirm_password", currentLanguage.value!!)

        getLabelByTagAndLanguage("error_invalid_password", currentLanguage.value!!)
        getLabelByTagAndLanguage("error_passwords_match", currentLanguage.value!!)
        getLabelByTagAndLanguage("error_field_required", currentLanguage.value!!)
        getLabelByTagAndLanguage("error_invalid_email", currentLanguage.value!!)

        _status.value = null
        email.value = ""
        password.value = ""
        repassword.value = ""
        firstName.value = ""
        lastname.value = ""
    }


    fun passwordMatch(): Boolean {
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

    fun onClickShowPassword(id: Int) {
        when (id) {
            1 -> {
                if (_showPassword1.value != null)
                    _showPassword1.value = !_showPassword1.value!!
                else {
                    _showPassword1.value = true
                }
            }
            2 -> {
                if (_showPassword2.value != null)
                    _showPassword2.value = !_showPassword2.value!!
                else {
                    _showPassword2.value = true
                }
            }
        }
    }


    fun register(credentials: RegistrationRequestModel) {
        viewModelScope.launch {
            // Get the Deferred object for our Retrofit request
           // var deferredRegistration = WandrApi.RETROFIT_SERVICE.register(credentials)

            // Await the completion of our Retrofit request
            try {
                _status.value = CallAndStatus(WandrApiStatus.LOADING, WandrApiRequestId.REGISTER)
            //    deferredRegistration.await()
                _status.value = CallAndStatus(WandrApiStatus.DONE, WandrApiRequestId.REGISTER)
            } catch (ex: HttpException) {
                _status.value = CallAndStatus(WandrApiStatus.ERROR, WandrApiRequestId.REGISTER)
                _error.value =
                    " " + ex.response().code() + " " + ex.response().errorBody()?.string()
            }
        }
    }

//    fun login(credentials: LoginRequestModel) {
//        viewModelScope.launch {
//            // Get the Deferred object for our Retrofit request
//            var getTokenModel = WandrApi.RETROFIT_SERVICE.login(credentials)
//
//            // Await the completion of our Retrofit request
//            try {
//                _status.value = CallAndStatus(WandrApiStatus.LOADING, WandrApiRequestId.LOGIN)
//                _tokenModel.value = getTokenModel.await()
//                _status.value = CallAndStatus(WandrApiStatus.DONE, WandrApiRequestId.LOGIN)
//            } catch (ex: HttpException) {
//                _status.value = CallAndStatus(WandrApiStatus.ERROR, WandrApiRequestId.LOGIN)
//                _error.value =
//                    " " + ex.response().code() + " " + ex.response().errorBody()?.string()
//            }
//        }
//    }


    override fun onCleared() {
        super.onCleared()
        Log.i("RegisterViewModel", "DESTROYED")
        viewModelJob.cancel()
    }


    fun getLabelByTagAndLanguage(labelTag: String, languageTag: String) {
        ioScope.launch {
            val label = database.findlabelByTag(labelTag, languageTag)
            withContext(Dispatchers.Main) {
                when (labelTag) {
                    "email" -> _emailHint.value = label?.name
                    "first_name" -> _firstNameHint.value = label?.name
                    "last_name" -> _lastNameHint.value = label?.name
                    "password" -> _passwordHint.value = label?.name
                    "confirm_password" -> _confirmPasswordHint.value = label?.name
                    "error_passwords_match" -> _validationErrorPasswordMatch.value = label?.name
                    "error_invalid_password" -> _validationErrorInvalidPassword.value = label?.name
                    "error_field_required" -> _validationErrorFieldRequired.value = label?.name
                    "error_invalid_email" -> _validationErrorInvalidEmail.value = label?.name
                }
            }
        }
    }

    var onFocusChangeListener: OnFocusChangeListener = object : OnFocusChangeListener {

        override fun onFocusChange(view: View, isFocused: Boolean) {
            //Hide Keyboard
            focus.value = isFocused
        }
    }


}
