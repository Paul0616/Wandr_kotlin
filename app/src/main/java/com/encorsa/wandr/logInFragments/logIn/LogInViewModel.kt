package com.encorsa.wandr.logInFragments.logIn

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.encorsa.wandr.R
import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.models.LoginRequestModel
import com.encorsa.wandr.models.LoginResponseModel
import com.encorsa.wandr.models.RegistrationRequestModel
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.utils.DEFAULT_LANGUAGE
import com.encorsa.wandr.utils.Prefs
import com.encorsa.wandr.utils.TranslationsLogin
import com.encorsa.wandr.utils.Utilities
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.*
import retrofit2.HttpException

class LogInViewModel(
    app: Application,
    val database: WandrDatabaseDao,
    val googleSignInClient: GoogleSignInClient
) : AndroidViewModel(app) {

    private lateinit var firebaseAuth: FirebaseAuth
    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
    private val ioScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    private val prefs = Prefs(app.applicationContext)
    //API status of the most recent request
    private val _status = MutableLiveData<WandrApiStatus>()
    val status: LiveData<WandrApiStatus>
        get() = _status

    private val _logInWithGoogle = MutableLiveData<Boolean>()
    val logInWithGoogle: LiveData<Boolean>
        get() = _logInWithGoogle

    private val _registrationIfGoogleAccountNotFound = MutableLiveData<RegistrationRequestModel>()
    val registrationIfGoogleAccountNotFound: LiveData<RegistrationRequestModel>
        get() = _registrationIfGoogleAccountNotFound

    private val _currentlanguage = MutableLiveData<String>()
    val currentlanguage: LiveData<String>
        get() = _currentlanguage

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

    private val _translations = MutableLiveData<TranslationsLogin>(
        TranslationsLogin(
            app.getString(R.string.emailHint),
            app.getString(R.string.passwordHint),
            app.getString(R.string.registerButonText),
            app.getString(R.string.invalid_credentials),
            app.getString(R.string.error_field_required),
            app.getString(R.string.error_invalid_email),
            app.getString(R.string.error_email_not_confirmed)
        )
    )
    val translations: LiveData<TranslationsLogin>
        get() = _translations

    private val _userValidation = MutableLiveData<LoginRequestModel>()
    val userValidation: LiveData<LoginRequestModel>
        get() = _userValidation

    init {
        Log.i("LogInViewModel", "CREATED")
        Utilities.setLanguageConfig(app.applicationContext)
        firebaseAuth = FirebaseAuth.getInstance()
        Log.i("LogInViewModel", "Google user:${firebaseAuth.currentUser?.email}")
        if (firebaseAuth.currentUser != null)
            logOutGoogle()

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

    fun setLogInType(withGoogle: Boolean) {
        _logInWithGoogle.value = withGoogle
    }

    fun logOutGoogle() {
        _status.value = WandrApiStatus.LOADING
        firebaseAuth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            _status.value = null
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {

                val user = firebaseAuth.currentUser

                user?.let {
                    val email = it.email
                    val password = getPasswordForSignUp(it)
                    val credentials = LoginRequestModel(email = email!!, password = password)
                    _registrationIfGoogleAccountNotFound.value = RegistrationRequestModel(
                        email = email,
                        password = password,
                        rePassword = password,
                        firstName = getFirstNameForSignUp(it),
                        lastName = getLastNameForSignUp(it),
                        firebaseToken = prefs.firebaseToken!!
                    )
                    Log.i("LogInViewModel", "${credentials}")
                    login(credentials)
                }
                _status.value = null
                //startActivity(HomeActivity.getLaunchIntent(this))
            } else {
                val errorMap = HashMap<String, Any?>()
                errorMap.put("code", 400)
                errorMap.put("message", "Google login failed!")
                _error.value = errorMap
            }
        }
    }

    private fun getFirstNameForSignUp(user: FirebaseUser): String {
        val name = user.displayName
        val names = name!!.split(" ").toTypedArray()
        return names[0] ?: ""
    }

    private fun getLastNameForSignUp(user: FirebaseUser): String {
        val name = user.displayName
        val names = name!!.split(" ").toTypedArray()
        return if (names.size > 1) names[1] else ""
    }

    private fun  getPasswordForSignUp(user: FirebaseUser): String {
        val uid = user.uid
        return "${uid.substring(6).toLowerCase()}!${uid.substring(0,5).toUpperCase()}"
    }

    fun setStatus(status: WandrApiStatus){
        _status.value = status
    }

    fun logInInitiated() {
        _logInWithGoogle.value = null
    }

    fun onClickLogIn() {
        val loginUser = LoginRequestModel(email.value!!, password.value!!, false)
        _userValidation.value = loginUser
    }

    fun onClickShowPassword() {
        _showPassword.value = !(_showPassword.value ?: false)
    }

    fun login(credentials: LoginRequestModel) {

        viewModelScope.launch {
            // Get the Deferred object for our Retrofit request
            var getTokenModel = WandrApi.RETROFIT_SERVICE.login(credentials, false)

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

    /*  -----------------------------------
    *   LANGUAGE CHANGE:
    *   - get labels for current tag language
    *  ------------------------------------
    */
    fun getLabelsByLanguage(languageTag: String) {
        ioScope.launch {
            val email = database.findlabelByTag("email", languageTag)
            val password = database.findlabelByTag("password", languageTag)
            val register = database.findlabelByTag("action_sign_up_short", languageTag)
            val invalidCredential = database.findlabelByTag("invalid_credentials", languageTag)
            val fieldReq = database.findlabelByTag("error_field_required", languageTag)
            val invalidEmail = database.findlabelByTag("error_invalid_email", languageTag)
            val emailNotConf = database.findlabelByTag("email_not_confirmed_message", languageTag)
            withContext(Dispatchers.Main) {
                _translations.value = TranslationsLogin(
                    email = email?.name,
                    password = password?.name,
                    register = register?.name,
                    invalidCredentials = invalidCredential?.name,
                    fieldReq = fieldReq?.name,
                    invalidEmail = invalidEmail?.name,
                    emailNotConfirmed = emailNotConf?.name
                )
                Log.i(
                    "TRANSLATIONS",
                    "${_translations.value?.email} - " +
                            "${_translations.value?.password} -" +
                            " ${_translations.value?.register} -" +
                            " ${_translations.value?.invalidCredentials} -" +
                            " ${_translations.value?.fieldReq} -" +
                            " ${_translations.value?.invalidEmail} -" +
                            " ${_translations.value?.emailNotConfirmed}"
                )
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        Log.i("LogInViewModel", "DESTROYED")
        ioScope.cancel()
    }
}