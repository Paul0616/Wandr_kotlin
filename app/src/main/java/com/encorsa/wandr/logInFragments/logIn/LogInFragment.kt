package com.encorsa.wandr.logInFragments.logIn


import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.encorsa.wandr.MainActivity
import com.encorsa.wandr.R
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.databinding.FragmentLogInBinding
import com.encorsa.wandr.models.LoginRequestModel
import com.encorsa.wandr.models.RegistrationRequestModel
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.utils.DEBUG_MODE
import com.encorsa.wandr.utils.Prefs
import com.encorsa.wandr.utils.Utilities
import com.encorsa.wandr.utils.Utilities.errorAlert
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import java.util.*

class LogInFragment : Fragment() {

    var emailNotConfirmedMessage: String? = null
    var invalidCredentialsMessage: String? = null
    var validationErrorFieldRequired: String? = null
    var validationErrorInvalidEmail: String? = null
    var google_acount_not_registered_title: String? = null
    var google_acount_not_registered_message: String? = null
    var newGoogleUser: RegistrationRequestModel? = null

    val RC_SIGN_IN: Int = 1
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mGoogleSignInOptions: GoogleSignInOptions
    lateinit var viewModel: LogInViewModel
    var withGoogle: Boolean? = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val prefs = Prefs(requireNotNull(activity).applicationContext)
        val application = requireNotNull(activity).application
        val binding = FragmentLogInBinding.inflate(inflater)
        val dataSource = WandrDatabase.getInstance(application).wandrDatabaseDao
        configureGoogleSignIn()
        val viewModelFactory = LogInViewModelFactory(application, dataSource, mGoogleSignInClient)
        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(LogInViewModel::class.java)
        binding.setLifecycleOwner(this)
        binding.loginViewModel = viewModel

        (activity as AppCompatActivity).supportActionBar?.hide()


        binding.signUpButton.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                LogInFragmentDirections.actionLogInFragmentToViewUrlFragment(false, null)
            )
        )


        binding.signInWithGoogleButton.setOnClickListener {
            viewModel.setLogInType(true)

        }

        viewModel.logInWithGoogle.observe(this, Observer {
            Log.i("LogInFragment", "logInWithGoogle: $it")
            withGoogle = it
            if (!it)
                viewModel.onClickLogIn()
            else {
                viewModel.setStatus(WandrApiStatus.LOADING)
                val signInIntent: Intent = mGoogleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        })

        viewModel.registrationIfGoogleAccountNotFound.observe(this, Observer {
            newGoogleUser = it
        })

        viewModel.showPassword.observe(this, Observer {
            binding.showPassword.isSelected = it!!
            if (!it)
                binding.passwordEdit.setTransformationMethod(PasswordTransformationMethod())
            else
                binding.passwordEdit.setTransformationMethod(null)
            Log.i("LogInFragment", it.toString())
        })

        viewModel.currentlanguage.observe(this, Observer {
            viewModel.getLabelsByLanguage(it)
        })

        viewModel.translations.observe(this, Observer {
            binding.translation = it
            invalidCredentialsMessage = it.invalidCredentials
            validationErrorFieldRequired = it.fieldReq
            validationErrorInvalidEmail = it.invalidEmail
            emailNotConfirmedMessage = it.emailNotConfirmed
            google_acount_not_registered_title = "Cont neînregistrat"
            google_acount_not_registered_message = "Acest cont de Google nu este înregistrat în baza noastră de date. Vrei să faci asta acum?"
        })


        viewModel.status.observe(this, Observer {
            Log.i("LogInFragment", "Status changed to ${it?.toString()}")
            binding.loginButton.isEnabled = true
            binding.signInWithGoogleButton.isEnabled = true
            binding.signUpButton.isEnabled = true
            when (it) {
                WandrApiStatus.LOADING -> {
                    binding.progressBarLogIn.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = false
                    binding.signInWithGoogleButton.isEnabled = false
                    binding.signUpButton.isEnabled = false
                }
                WandrApiStatus.DONE -> {
                    binding.progressBarLogIn.visibility = View.INVISIBLE
                }
                WandrApiStatus.ERROR -> {
                    binding.progressBarLogIn.visibility = View.INVISIBLE
                    Log.i("LogInFragment", "ERROR")
                }
                else -> binding.progressBarLogIn.visibility = View.INVISIBLE
            }
        })

        viewModel.tokenModel.observe(this, Observer {
            prefs.userEmail = it?.email
            prefs.userId = it?.userId
            prefs.userName = it?.userName
            prefs.token = it?.token
            prefs.firstName = it?.firstName
            prefs.password = binding.passwordEdit.text.toString()

            val tokenExpireAt = Utilities.getLongDate(it?.tokenExpirationDate)
            if (null != tokenExpireAt)
                prefs.tokenExpireAtInMillis = tokenExpireAt
            startActivity(Intent(activity, MainActivity::class.java))
            (activity as AppCompatActivity).finish()
        })

        viewModel.error.observe(this, Observer {
            Log.i("LogInFragment", "Treating errors")
            if (it.contains("code")) {
                var positiveButtonClick = { _: DialogInterface, _: Int -> }
                if (it.get("code") == 403) {

                    if (invalidCredentialsMessage != null)
                        errorAlert(
                            activity as AppCompatActivity,
                            invalidCredentialsMessage!!,
                            false,
                            positiveButtonClick
                        )
                    else
                        errorAlert(
                            activity as AppCompatActivity,
                            getString(R.string.invalid_credentials),
                            false,
                            positiveButtonClick
                        )

                } else if (it.get("code") == 404) {
                    if (!withGoogle!!) {
                        Toast.makeText(
                            application.applicationContext,
                            "NOT FOUND",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else
                        alertDialog(google_acount_not_registered_title, google_acount_not_registered_message)
                } else if (it.get("code") == 417) {
                    positiveButtonClick = { _: DialogInterface, _: Int ->
                        prefs.userEmail = binding.emailEdit.text.toString()
                        prefs.password = binding.passwordEdit.text.toString()
                        this.findNavController().navigate(
                            LogInFragmentDirections.actionLogInFragmentToCheckEmailFragment()
                        )
                    }
                    if (emailNotConfirmedMessage != null)
                        errorAlert(
                            activity as AppCompatActivity,
                            emailNotConfirmedMessage!!,
                            true,
                            positiveButtonClick
                        )
                    else
                        errorAlert(
                            activity as AppCompatActivity,
                            getString(R.string.error_email_not_confirmed),
                            true,
                            positiveButtonClick
                        )
                } else {
                    if (DEBUG_MODE)
                        Toast.makeText(
                            application.applicationContext,
                            it.get("message").toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }


//            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
//                .show()
//            if (DEBUG_MODE)
//                Toast.makeText(
//                    application.applicationContext,
//                    when (it) {
//                        (null) -> ""
//                        else -> it
//                    },
//                    Toast.LENGTH_LONG
//                ).show()
        })



        viewModel.userValidation.observe(this, Observer {
            Log.i("LogInFragment", "Validating user")
            if (TextUtils.isEmpty(Objects.requireNonNull<LoginRequestModel>(it).email)) {
                if (validationErrorFieldRequired != null)
                    binding.emailEdit.error = validationErrorFieldRequired
                else
                    binding.emailEdit.error = getString(R.string.error_field_required)
                binding.emailEdit.requestFocus()
            } else if (!it.isEmailValid) {
                if (validationErrorInvalidEmail != null)
                    binding.emailEdit.error = validationErrorInvalidEmail
                else
                    binding.emailEdit.error = getString(R.string.error_invalid_email)
                binding.emailEdit.requestFocus()
            } else if (TextUtils.isEmpty(Objects.requireNonNull(it).password)) {
                if (validationErrorFieldRequired != null)
                    binding.passwordEdit.error = validationErrorFieldRequired
                else
                    binding.passwordEdit.error = getString(R.string.error_field_required)
                binding.passwordEdit.requestFocus()
            } else {
                viewModel.login(it)

            }

        })

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.let {
                    viewModel.firebaseAuthWithGoogle(it)
                }
            } catch (e: ApiException) {
                if (DEBUG_MODE)
                    Toast.makeText(
                        (activity as AppCompatActivity).applicationContext,
                        "Google sign in failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Navigation.findNavController(view).getCurrentDestination()
            ?.setLabel(getString(R.string.app_name));
    }

    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient =
            GoogleSignIn.getClient(activity as AppCompatActivity, mGoogleSignInOptions)
    }

    fun alertDialog(title: String?, msg: String?) {
        val builder =
            AlertDialog.Builder(activity as AppCompatActivity)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setCancelable(false)
        builder.setPositiveButton(
            "Ok"
        ) { dialog, which ->

            findNavController().navigate(LogInFragmentDirections.actionLogInFragmentToViewUrlFragment(true, newGoogleUser))
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

}