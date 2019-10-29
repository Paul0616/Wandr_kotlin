package com.encorsa.wandr.logInFragments.register


import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.TextUtils
import android.util.Log

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.encorsa.wandr.MainActivity
import com.encorsa.wandr.databinding.FragmentRegisterBinding
import com.encorsa.wandr.network.WandrApiRequestId
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.network.models.LoginRequestModel
import com.encorsa.wandr.network.models.RegistrationRequestModel
import com.encorsa.wandr.utils.DEBUG_MODE
import com.encorsa.wandr.utils.Prefs
import com.encorsa.wandr.utils.Utilities
import java.util.*


class RegisterFragment : Fragment() {

    companion object {
        fun newInstance() = RegisterFragment()
    }

    private lateinit var viewModel: RegisterViewModel
    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater)
        binding.setLifecycleOwner(this)
        (activity as AppCompatActivity).supportActionBar?.show()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val application = requireNotNull(activity).application
        val prefs = Prefs(requireNotNull(activity).applicationContext)
        viewModel = ViewModelProviders.of(this).get(RegisterViewModel::class.java)
        binding.registerViewModel = viewModel
        // TODO: Use the ViewModel

        binding.passwordInfo.setVisibility(View.GONE)

        binding.repasswordEdit.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.passwordInfo.setVisibility(View.GONE)
                return@OnEditorActionListener false

            }
            false
        })

        viewModel.focus.observe(this, Observer {
            Log.i("RegisterFragment", it.toString())
            if (it)
                binding.passwordInfo.setVisibility(View.VISIBLE)
        })

        viewModel.status.observe(this, Observer {
            when (it?.status) {
                WandrApiStatus.LOADING -> {
                    binding.progressBarRegister.visibility = View.VISIBLE
                    when (it.requestId) {
                        WandrApiRequestId.REGISTER -> {
                            Log.i("RegisterFragment", "LOADING REGISTER")
                        }
                        WandrApiRequestId.LOGIN -> {
                            Log.i("RegisterFragment", "LOADING LOGIN")
                        }
                        else -> Log.i("RegisterFragment", "UNKNOWN")
                    }
                }

                WandrApiStatus.DONE -> {
                    binding.progressBarRegister.visibility = View.INVISIBLE
                    when (it.requestId) {
                        WandrApiRequestId.REGISTER -> {
                            Log.i("RegisterFragment", "DONE REGISTER")
                            val tokenRequestModel = LoginRequestModel(
                                binding.emailEdit.text.toString(),
                                binding.passwordEdit.text.toString()
                            )
                            viewModel.login(tokenRequestModel)
                        }
                        WandrApiRequestId.LOGIN -> {
                            Log.i("RegisterFragment", "DONE LOGIN")
                        }
                        else -> Log.i("RegisterFragment", "UNKNOWN")
                    }
                }
                WandrApiStatus.ERROR -> {
                    binding.progressBarRegister.visibility = View.INVISIBLE
                    when (it.requestId) {
                        WandrApiRequestId.REGISTER -> {
                            Log.i("RegisterFragment", "ERROR REGISTER")
                        }
                        WandrApiRequestId.LOGIN -> {
                            Log.i("RegisterFragment", "ERROR LOGIN")
                        }
                        else -> Log.i("RegisterFragment", "UNKNOWN")
                    }
                }
                else -> binding.progressBarRegister.visibility = View.INVISIBLE
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
            if (TextUtils.isEmpty(Objects.requireNonNull<RegistrationRequestModel>(it).email)) {
                binding.emailEdit.error = "Enter an E-Mail Address"
                binding.emailEdit.requestFocus()
            } else if (!it.isEmailValid) {
                binding.emailEdit.error = "Enter a Valid E-mail Address"
                binding.emailEdit.requestFocus()
            } else if (TextUtils.isEmpty(Objects.requireNonNull(it).firstName)) {
                binding.firstNameEdit.error = "Enter your First Name"
                binding.firstNameEdit.requestFocus()
            } else if (TextUtils.isEmpty(Objects.requireNonNull(it).lastName)) {
                binding.lastNameEdit.error = "Enter your Last Name"
                binding.lastNameEdit.requestFocus()
            } else if (TextUtils.isEmpty(Objects.requireNonNull(it).password)) {
                binding.passwordEdit.error = "Enter a Password"
                binding.passwordEdit.requestFocus()
            } else if (!it.isPasswordValid()) {
                binding.passwordEdit.error = "Password invalid"
                binding.passwordEdit.requestFocus()
            } else if (!viewModel.passwordMatch()) {
                binding.repasswordEdit.error = "Passwords do not match"
                binding.repasswordEdit.requestFocus()
            } else {
                viewModel.register(it)
            }
        })
    }

}
