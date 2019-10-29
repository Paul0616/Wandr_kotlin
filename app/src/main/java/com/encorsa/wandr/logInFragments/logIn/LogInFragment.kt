package com.encorsa.wandr.logInFragments.logIn

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.encorsa.wandr.MainActivity
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.network.models.LoginRequestModel
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.databinding.FragmentLogInBinding
import com.encorsa.wandr.utils.DEBUG_MODE
import com.encorsa.wandr.utils.Prefs
import com.encorsa.wandr.utils.Utilities

import java.util.*

class LogInFragment : Fragment() {


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
                LogInFragmentDirections.actionLogInFragmentToViewUrlFragment().setTitle(
                    "XXX"
                )
            )
        )

        viewModel.currentlanguage.observe(this, Observer {

            binding.emailEdit.hint = viewModel.getLabelByTagAndLanguage("email", it) ?:
            Log.i("LogInFragment", binding.emailEdit.hint)
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
        })

        viewModel.error.observe(this, Observer {
            if (DEBUG_MODE)
                Toast.makeText(
                    application.applicationContext,
                    when (it) {
                        (null) -> ""
                        else -> it
                    },
                    Toast.LENGTH_LONG
                ).show()
        })



        viewModel.userValidation.observe(this, Observer {
            if (TextUtils.isEmpty(Objects.requireNonNull<LoginRequestModel>(it).email)) {
                binding.emailEdit.error = "Enter an E-Mail Address"
                binding.emailEdit.requestFocus()
            } else if (!it.isEmailValid) {
                binding.emailEdit.error = "Enter a Valid E-mail Address"
                binding.emailEdit.requestFocus()
            } else if (TextUtils.isEmpty(Objects.requireNonNull(it).password)) {
                binding.passwordEdit.error = "Enter a Password"
                binding.passwordEdit.requestFocus()
            } else if (!it.isPasswordLengthGreaterThan4) {
                binding.passwordEdit.error = "Enter at least 6 Digit password"
                binding.passwordEdit.requestFocus()
            } else {
                viewModel.login(it)

            }
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Navigation.findNavController(view).getCurrentDestination()?.setLabel("Hello");
    }


}