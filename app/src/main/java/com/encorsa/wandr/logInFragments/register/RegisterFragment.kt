package com.encorsa.wandr.logInFragments.register


import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.TextUtils
import android.util.Log

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.encorsa.wandr.databinding.FragmentRegisterBinding
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.network.models.RegistrationRequestModel
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
        viewModel = ViewModelProviders.of(this).get(RegisterViewModel::class.java)
        binding.registerViewModel = viewModel
        // TODO: Use the ViewModel
//        val editTextList = ArrayList<EditText>()
//        editTextList.add(binding.emailEdit)
//        editTextList.add(binding.firstNameEdit)
//        editTextList.add(binding.lastNameEdit)
//        editTextList.add(binding.passwordEdit)
//        editTextList.add(binding.repasswordEdit)
        binding.passwordInfo.setVisibility(View.GONE)
//        for (view in editTextList) {
//            view.setOnFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
//                if (hasFocus) {
//                    val focusedView = v as EditText
//                    if (focusedView === binding.passwordEdit || focusedView === binding.repasswordEdit) {
//                        binding.passwordInfo.setVisibility(View.VISIBLE)
//                    }
//                } else {
//                    binding.passwordInfo.setVisibility(View.GONE)
//                }
//            })
//        }

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
            when (it) {
                WandrApiStatus.LOADING -> binding.progressBarRegister.visibility = View.VISIBLE
                WandrApiStatus.DONE -> {
                    binding.progressBarRegister.visibility = View.INVISIBLE
                }
                WandrApiStatus.ERROR -> {
                    binding.progressBarRegister.visibility = View.INVISIBLE
                    Log.i("RegisterFragment", "ERROR")
                }
                else -> binding.progressBarRegister.visibility = View.INVISIBLE
            }
        })



        viewModel.error.observe(this, Observer {
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
