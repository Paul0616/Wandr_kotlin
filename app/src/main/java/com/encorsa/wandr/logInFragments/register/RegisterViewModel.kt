package com.encorsa.wandr.logInFragments.register

import android.app.Application
import android.util.Log
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.network.WandrApiStatus

import com.encorsa.wandr.models.RegistrationRequestModel
import retrofit2.HttpException
import android.view.View.OnFocusChangeListener
import android.view.View
import com.encorsa.wandr.R
import androidx.lifecycle.*
import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.network.CallAndStatus
import com.encorsa.wandr.network.WandrApiRequestId
import com.encorsa.wandr.models.SecurityCode
import com.encorsa.wandr.utils.DEFAULT_LANGUAGE
import com.encorsa.wandr.utils.Prefs
import com.encorsa.wandr.utils.TranslationsRegistration
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

    private val _securityCode = MutableLiveData<SecurityCode>()
    val securityCode: LiveData<SecurityCode>
        get() = _securityCode



    private val _showPassword1 = MutableLiveData<Boolean>()
    val showPassword1: LiveData<Boolean>
        get() = _showPassword1

    private val _showPassword2 = MutableLiveData<Boolean>()
    val showPassword2: LiveData<Boolean>
        get() = _showPassword2


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
        val currentLanguage = prefs.currentLanguage.let {
            it ?: DEFAULT_LANGUAGE
        }
        getLabelsByLanguage(currentLanguage)

        _status.value = null
        email.value = ""
        password.value = ""
        repassword.value = ""
        firstName.value = ""
        lastname.value = ""
    }

    private val _translations = MutableLiveData<TranslationsRegistration>(
        TranslationsRegistration(
            app.getString(R.string.emailHint),
            app.getString(R.string.passwordHint),
            app.getString(R.string.firstNameHint),
            app.getString(R.string.lastNameHint),
            app.getString(R.string.confirmPasswordHint),
            app.getString(R.string.error_invalid_password),
            app.getString(R.string.error_password_match),
            app.getString(R.string.error_field_required),
            app.getString(R.string.error_invalid_email),
            app.getString(R.string.password_info_title),
            app.getString(R.string.password_info_number),
            app.getString(R.string.password_info_case),
            app.getString(R.string.password_info_lenght),
            app.getString(R.string.password_info_special_char),
            app.getString(R.string.registerButonText)
        )
    )
    val translations: LiveData<TranslationsRegistration>
        get() = _translations

    fun clearStatus(){
        _status.value = null
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
            lastname.value!!,
            prefs.firebaseToken!!
        )
        _userValidation.value = registerUser

        //uncomment if you want to skip validation andregistering
        //_status.value = CallAndStatus(WandrApiStatus.DONE, WandrApiRequestId.REGISTER)
    }

    fun onClickShowPassword(id: Int) {
        when (id) {
            1 -> _showPassword1.value = !(_showPassword1.value ?: false)
            2 -> _showPassword2.value = !(_showPassword2.value ?: false)
        }
    }


    fun register(credentials: RegistrationRequestModel) {
        viewModelScope.launch {
            // Get the Deferred object for our Retrofit request
            var deferredRegistration = WandrApi.RETROFIT_SERVICE.register(credentials)

            // Await the completion of our Retrofit request
            try {
                _status.value = CallAndStatus(WandrApiStatus.LOADING, WandrApiRequestId.REGISTER)
                _securityCode.value = deferredRegistration.await()
                _status.value = CallAndStatus(WandrApiStatus.DONE, WandrApiRequestId.REGISTER)
            } catch (ex: HttpException) {
                _status.value = CallAndStatus(WandrApiStatus.ERROR, WandrApiRequestId.REGISTER)
                _error.value =
                    " " + ex.response().code() + " " + ex.response().errorBody()?.string()
            }
        }
    }



    override fun onCleared() {
        super.onCleared()
        Log.i("RegisterViewModel", "DESTROYED")
        viewModelJob.cancel()
    }

    fun getLabelsByLanguage(languageTag: String) {
        ioScope.launch {
            val email = database.findlabelByTag("email", languageTag)
            val password = database.findlabelByTag("password", languageTag)
            val firstName = database.findlabelByTag("first_name", languageTag)
            val lastName = database.findlabelByTag("last_name", languageTag)
            val confirmPassword = database.findlabelByTag("confirm_password", languageTag)

            val errorInvalidPassword = database.findlabelByTag("error_invalid_password", languageTag)
            val errorPasswordsMatch = database.findlabelByTag("error_passwords_match", languageTag)
            val errorFieldRequred = database.findlabelByTag("error_field_required", languageTag)
            val errorInvalidEmail = database.findlabelByTag("error_invalid_email", languageTag)

            val passwordMessage1 = database.findlabelByTag("password_info_message1", languageTag)
            val passwordMessage2 = database.findlabelByTag("password_info_message2", languageTag)
            val passwordMessage3 = database.findlabelByTag("password_info_message3", languageTag)
            val passwordMessage4 = database.findlabelByTag("password_info_message4", languageTag)
            val passwordMessage5 = database.findlabelByTag("password_info_message5", languageTag)

            val screenTitle = database.findlabelByTag("action_sign_up_short", languageTag)
            withContext(Dispatchers.Main) {
                _translations.value = TranslationsRegistration(
                    email = email?.name,
                    password = password?.name,
                    firstName = firstName?.name,
                    lastName = lastName?.name,
                    confirmPassword = confirmPassword?.name,
                    errorInvalidPassword = errorInvalidPassword?.name,
                    errorFieldRequired = errorFieldRequred?.name,
                    errorPasswordsMatch = errorPasswordsMatch?.name,
                    errorInvalidEmail = errorInvalidEmail?.name,
                    passwordMessage1 = passwordMessage1?.name,
                    passwordMessage2 = passwordMessage2?.name,
                    passwordMessage3 = passwordMessage3?.name,
                    passwordMessage4 = passwordMessage4?.name,
                    passwordMessage5 = passwordMessage5?.name,
                    screenTitle = screenTitle?.name
                )
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
