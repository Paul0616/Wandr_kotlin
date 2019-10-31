package com.encorsa.wandr.logInFragments.register


import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
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
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.encorsa.wandr.MainActivity
import com.encorsa.wandr.R
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.databinding.FragmentRegisterBinding
import com.encorsa.wandr.logInFragments.logIn.LogInFragmentDirections
import com.encorsa.wandr.logInFragments.logIn.LogInViewModelFactory
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


    var validationErrorFieldRequired: String? = null
    var validationErrorInvalidEmail: String? = null
    var validationErrorInvalidPassword: String? = null
    var validationErrorPasswordMatch: String? = null

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
        val dataSource = WandrDatabase.getInstance(application).wandrDatabaseDao
        val prefs = Prefs(application.applicationContext)
        val viewModelFactory = RegisterViewModelFactory(application, dataSource)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(RegisterViewModel::class.java)
        binding.registerViewModel = viewModel


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
            else
                binding.passwordInfo.setVisibility(View.GONE)
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
                            prefs.logOut()
                            prefs.userEmail = binding.emailEdit.text.toString()
                            prefs.password = binding.passwordEdit.text.toString()
                            prefs.securityCode =
                            this.findNavController().navigate(
                                RegisterFragmentDirections.actionRegisterFragmentToCheckEmailFragment()
                            )

                        }
                        WandrApiRequestId.LOGIN -> {
                            Log.i("RegisterFragment", "DONE LOGIN")
                        }
                        else -> Log.i("RegisterFragment", "UNKNOWN")
                    }
                    viewModel.clearStatus()
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


        viewModel.showPassword1.observe(this, Observer {
            binding.showPassword1.isSelected = it!!
            if (!it)
                binding.passwordEdit.setTransformationMethod(PasswordTransformationMethod())
            else
                binding.passwordEdit.setTransformationMethod(null)
        })

        viewModel.showPassword2.observe(this, Observer {
            binding.showPassword2.isSelected = it!!
            if (!it)
                binding.repasswordEdit.setTransformationMethod(PasswordTransformationMethod())
            else
                binding.repasswordEdit.setTransformationMethod(null)
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


        viewModel.emailHint.observe(this, Observer {
            if (it != null)
                binding.emailEdit.hint = it
        })

        viewModel.passwordHint.observe(this, Observer {
            if (it != null)
                binding.passwordEdit.hint = it
        })

        viewModel.firstNameHint.observe(this, Observer {
            if (it != null)
                binding.firstNameEdit.hint = it
        })

        viewModel.lastNameHint.observe(this, Observer {
            if (it != null)
                binding.lastNameEdit.hint = it
        })
        viewModel.confirmPasswordHint.observe(this, Observer {
            if (it != null)
                binding.repasswordEdit.hint = it
        })

        viewModel.validationErrorFieldRequired.observe(this, Observer {
            this.validationErrorFieldRequired = it
        })

        viewModel.validationErrorInvalidEmail.observe(this, Observer {
            this.validationErrorInvalidEmail = it
        })

        viewModel.validationErrorInvalidPassword.observe(this, Observer {
            this.validationErrorInvalidPassword = it
        })

        viewModel.validationErrorPasswordMatch.observe(this, Observer {
            this.validationErrorPasswordMatch = it
        })

        viewModel.userValidation.observe(this, Observer {
            if (TextUtils.isEmpty(Objects.requireNonNull<RegistrationRequestModel>(it).email)) {
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

            else if (TextUtils.isEmpty(Objects.requireNonNull(it).firstName)) {
                if (validationErrorFieldRequired != null)
                    binding.firstNameEdit.error = validationErrorFieldRequired
                else
                    binding.firstNameEdit.error = getString(R.string.error_field_required)
                binding.firstNameEdit.requestFocus()
            }

            else if (TextUtils.isEmpty(Objects.requireNonNull(it).lastName)) {
                if (validationErrorFieldRequired != null)
                    binding.lastNameEdit.error = validationErrorFieldRequired
                else
                    binding.lastNameEdit.error = getString(R.string.error_field_required)
                binding.lastNameEdit.requestFocus()
            }

            else if (TextUtils.isEmpty(Objects.requireNonNull(it).password)) {
                if (validationErrorFieldRequired != null)
                    binding.passwordEdit.error = validationErrorFieldRequired
                else
                    binding.passwordEdit.error = getString(R.string.error_field_required)
                binding.passwordEdit.requestFocus()
            }

            else if (!it.isPasswordValid()) {
                if (validationErrorInvalidPassword != null)
                    binding.passwordEdit.error = validationErrorInvalidPassword
                else
                    binding.passwordEdit.error = getString(R.string.error_invalid_password)
                binding.passwordEdit.requestFocus()
            }

            else if (!viewModel.passwordMatch()) {
                if (validationErrorPasswordMatch != null)
                    binding.repasswordEdit.error = validationErrorPasswordMatch
                else
                    binding.repasswordEdit.error = getString(R.string.error_password_match)
                binding.repasswordEdit.requestFocus()
            }

            else {
                viewModel.register(it)
            }
        })
    }

}
