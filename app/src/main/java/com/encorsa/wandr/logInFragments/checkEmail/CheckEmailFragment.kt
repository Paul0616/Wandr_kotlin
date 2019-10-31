package com.encorsa.wandr.logInFragments.checkEmail

import android.content.Intent
import android.graphics.Color
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.encorsa.wandr.MainActivity
import com.encorsa.wandr.R
import com.encorsa.wandr.databinding.FragmentCheckEmailBinding
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.network.models.LoginRequestModel
import com.encorsa.wandr.utils.DEBUG_MODE
import com.encorsa.wandr.utils.Prefs
import com.encorsa.wandr.utils.Utilities
import com.encorsa.wandr.utils.Utilities.errorAlert


class CheckEmailFragment : Fragment() {

    companion object {
        fun newInstance() = CheckEmailFragment()
    }

    private lateinit var viewModel: CheckEmailViewModel
    private lateinit var binding: FragmentCheckEmailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCheckEmailBinding.inflate(inflater)
        binding.setLifecycleOwner(this)
        (activity as AppCompatActivity).supportActionBar?.show()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val prefs = Prefs(activity!!.applicationContext)
        viewModel = ViewModelProviders.of(this).get(CheckEmailViewModel::class.java)

        binding.checkEmailViewModel = viewModel

        binding.currentEmailEdit.setVisibility(View.GONE)
        binding.currentEmail.text = prefs.userEmail ?: ""

        viewModel.emailMustBeEdited.observe(this, Observer {
            binding.apply {
                editEmailButton.isSelected = it!!
                if (it) {
                    currentEmail.visibility = View.GONE
                    currentEmailEdit.setText(currentEmail.text)
                    currentEmailEdit.visibility = View.VISIBLE
                    currentEmailEdit.requestFocus()
                } else {
                    currentEmail.text = currentEmailEdit.text.toString()
                    currentEmailEdit.visibility = View.GONE
                    currentEmail.visibility = View.VISIBLE
                }
            }
        })

        viewModel.validateSecurityCode.observe(this, Observer {
            if (it == prefs.securityCode){ //trebuie comparat cu pref.securityCode
                //TODO: userul trebuie logat
                val credentials = LoginRequestModel(prefs.userEmail ?: "", prefs.password ?: "")
                viewModel.login(credentials)
//                startActivity(Intent(activity, MainActivity::class.java))
            } else {
                errorAlert(activity as AppCompatActivity, getString(R.string.wrong_security_code))
            }
        })

        viewModel.status.observe(this, Observer {
            when (it) {
                WandrApiStatus.LOADING -> binding.progressBarCheckEmail.visibility = View.VISIBLE
                WandrApiStatus.DONE -> {
                    binding.progressBarCheckEmail.visibility = View.INVISIBLE
                }
                WandrApiStatus.ERROR -> {
                    binding.progressBarCheckEmail.visibility = View.INVISIBLE
                    Log.i("LogInFragment", "ERROR")
                }
                else -> binding.progressBarCheckEmail.visibility = View.INVISIBLE
            }
            //viewModel.clearStatus()
        })

        viewModel.tokenModel.observe(this, Observer {
            prefs.userEmail = it?.email
            prefs.userId = it?.userId
            prefs.userName = it?.userName
            prefs.token = it?.token
            prefs.firstName = it?.firstName

            val tokenExpireAt = Utilities.getLongDate(it?.tokenExpirationDate)
            if (null != tokenExpireAt)
                prefs.tokenExpireAtInMillis = tokenExpireAt
            startActivity(Intent(activity, MainActivity::class.java))
            (activity as AppCompatActivity).finish()
        })

        viewModel.error.observe(this, Observer {
            if (it.contains("code") && it.contains("message")) {
                errorAlert(
                    activity as AppCompatActivity,
                    it.get("message").toString()
                )

            }
        })
    }

}
