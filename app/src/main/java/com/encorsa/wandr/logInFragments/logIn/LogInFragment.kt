package com.encorsa.wandr.logInFragments.logIn

import android.content.Context
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
import com.encorsa.wandr.MainActivity
import com.encorsa.wandr.R
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.network.models.LoginRequestModel
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.databinding.FragmentLogInBinding
import com.encorsa.wandr.utils.DEBUG_MODE
import com.encorsa.wandr.utils.Prefs
import com.encorsa.wandr.utils.Utilities
import com.encorsa.wandr.utils.Utilities.errorAlert


import java.util.*

class LogInFragment : Fragment() {

    var invalidCredentialsMessage: String? = null
    var validationErrorFieldRequired: String? = null
    var validationErrorInvalidEmail: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val prefs = Prefs(requireNotNull(activity).applicationContext)
        val application = requireNotNull(activity).application
        val binding = FragmentLogInBinding.inflate(inflater)
        val dataSource = WandrDatabase.getInstance(application).wandrDatabaseDao
        val viewModelFactory = LogInViewModelFactory(application, dataSource)
        val viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(LogInViewModel::class.java)
        binding.setLifecycleOwner(this)
        binding.loginViewModel = viewModel
        // binding.infoText.text = "Welcome to Log In Fragment"

        (activity as AppCompatActivity).supportActionBar?.hide()


        binding.signUpButton.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                LogInFragmentDirections.actionLogInFragmentToViewUrlFragment()
            )
        )

        viewModel.showPassword.observe(this, Observer {
            binding.showPassword.isSelected = it!!
            if (!it)
                binding.passwordEdit.setTransformationMethod(PasswordTransformationMethod())
            else
                binding.passwordEdit.setTransformationMethod(null)
            Log.i("LogInFragment", it.toString())
        })

        viewModel.currentlanguage.observe(this, Observer {
            viewModel.getLabelByTagAndLanguage("email", it)
            viewModel.getLabelByTagAndLanguage("password", it)
            //viewModel.getLabelByTagAndLanguage("action_sign_in_short", it)
            viewModel.getLabelByTagAndLanguage("action_sign_up_short", it)
            viewModel.getLabelByTagAndLanguage("invalid_credentials", it)
            viewModel.getLabelByTagAndLanguage("error_field_required", it)
            viewModel.getLabelByTagAndLanguage("error_invalid_email", it)

        })

        viewModel.emailHint.observe(this, Observer {
            if (it != null)
                binding.emailEdit.hint = it
        })

        viewModel.passwordHint.observe(this, Observer {
            if (it != null)
                binding.passwordEdit.hint = it
        })

        viewModel.registerButtonText.observe(this, Observer {
            if (it != null)
                binding.signUpButton.text = it
        })

        viewModel.invalidCredentials.observe(this, Observer {
            invalidCredentialsMessage = it
        })

        viewModel.validationErrorFieldRequired.observe(this, Observer {
            this.validationErrorFieldRequired = it
        })

        viewModel.validationErrorInvalidEmail.observe(this, Observer {
            this.validationErrorInvalidEmail = it
        })

        viewModel.status.observe(this, Observer {
            when (it) {
                WandrApiStatus.LOADING -> binding.progressBarLogIn.visibility = View.VISIBLE
                WandrApiStatus.DONE -> {
                    binding.progressBarLogIn.visibility = View.INVISIBLE
                }
                WandrApiStatus.ERROR -> {
                    binding.progressBarLogIn.visibility = View.INVISIBLE
                    Log.i("LogInFragment", "ERROR")
                }
                else -> binding.progressBarLogIn.visibility = View.INVISIBLE
            }
            //viewModel.clearStatus()
        })

        viewModel.tokenModel.observe(this, Observer {
            if (DEBUG_MODE)
                Toast.makeText(application.applicationContext, it?.token, Toast.LENGTH_SHORT).show()
//            binding.infoText.text = it?.token?.length.toString()
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
            if (it.contains("code")) {
                if (it.get("code") == 403) {
                    if (invalidCredentialsMessage != null)
                        errorAlert(activity as AppCompatActivity, invalidCredentialsMessage!!)
                    else
                        errorAlert(
                            activity as AppCompatActivity,
                            getString(R.string.invalid_credentials)
                        )
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

            if (TextUtils.isEmpty(Objects.requireNonNull<LoginRequestModel>(it).email)) {
                if (validationErrorFieldRequired != null)
                    binding.emailEdit.error = validationErrorFieldRequired
                else
                    binding.emailEdit.error = getString(R.string.error_field_required)
                binding.emailEdit.requestFocus()
            }

            else if (!it.isEmailValid) {
                if (validationErrorInvalidEmail != null)
                    binding.emailEdit.error = validationErrorInvalidEmail
                else
                    binding.emailEdit.error = getString(R.string.error_invalid_email)
                binding.emailEdit.requestFocus()
            }

            else if (TextUtils.isEmpty(Objects.requireNonNull(it).password)) {
                if (validationErrorFieldRequired != null)
                    binding.passwordEdit.error = validationErrorFieldRequired
                else
                    binding.passwordEdit.error = getString(R.string.error_field_required)
                binding.passwordEdit.requestFocus()
            }

            else {
                viewModel.login(it)

            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Navigation.findNavController(view).getCurrentDestination()
            ?.setLabel(getString(R.string.app_name));
    }


//    fun errorAlert(context: Context, message: String) {
//        val positiveButtonClick = { _: DialogInterface, _: Int -> }
//        val builder = AlertDialog.Builder(context)
//
//        with(builder)
//        {
//            setTitle(getString(R.string.app_name))
//            setMessage(message)
//            setCancelable(false)
//            setPositiveButton("OK", DialogInterface.OnClickListener(positiveButtonClick))
//            show()
//        }
//
//
//    }
}