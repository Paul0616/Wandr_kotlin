package com.encorsa.wandr.logInFragments.logIn

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.encorsa.wandr.MainActivity
import com.encorsa.wandr.network.models.LoginRequestModel
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.databinding.FragmentLogInBinding
import java.util.*

class LogInFragment : Fragment() {

    private var credentials: LoginRequestModel = LoginRequestModel("", "", false)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentLogInBinding.inflate(inflater)
        // val credentials: LoginRequestModel = LoginRequestModel("", "", false)
        val viewModelFactory = LogInViewModelFactory(credentials)
        val viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(LogInViewModel::class.java)
        binding.setLifecycleOwner(this)
        binding.loginViewModel = viewModel
        //binding.loginRequest = credentials
        binding.infoText.text = "Welcome to Log In Fragment"
        binding.loginButton.setOnClickListener {
            //viewModel.lo
            val cred: LoginRequestModel = LoginRequestModel(
                binding.emailEdit.text.toString(),
                binding.passwordEdit.text.toString(),
                false
            )
            viewModel.login(cred)
        }
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
            binding.infoText.text = it?.token?.length.toString()
            startActivity(Intent(activity, MainActivity::class.java))
        })
        viewModel.error.observe(this, Observer {
            binding.infoText.text = when (it) {
                (null) -> ""
                else -> it
            }
        })

        viewModel.userRequest.observe(this, Observer {
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
}