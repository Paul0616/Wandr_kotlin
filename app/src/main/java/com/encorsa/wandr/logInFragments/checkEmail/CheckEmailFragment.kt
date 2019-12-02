package com.encorsa.wandr.logInFragments.checkEmail

import android.content.DialogInterface
import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.encorsa.wandr.MainActivity
import com.encorsa.wandr.R
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.databinding.FragmentCheckEmailBinding
import com.encorsa.wandr.network.WandrApiRequestId
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.models.LoginRequestModel
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
    var errorWrongSecurityCode: String? = null
    var errorEmailNoChange: String? = null

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
        val application = requireNotNull(activity).application
        val dataSource = WandrDatabase.getInstance(application).wandrDatabaseDao
        val viewModelFactory = CheckEmailModelFactory(application, dataSource)
        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(CheckEmailViewModel::class.java)

        binding.checkEmailViewModel = viewModel

        binding.currentEmailEdit.setVisibility(View.GONE)
        binding.currentEmail.text = prefs.userEmail ?: ""
        binding.progressBarCheckEmail.visibility = View.GONE

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
                    Log.i("CheckEmailFragment", "VALIDATE NEW EMAIL")
                    if (Patterns.EMAIL_ADDRESS.matcher(viewModel.newEmail.value as CharSequence).matches()) {
                        currentEmailEdit.visibility = View.GONE
                        currentEmail.visibility = View.VISIBLE
                        Log.i("CheckEmailFragment", "UPDATE to ${viewModel.newEmail.value}")
                        viewModel.onEditEmailDone(prefs.userEmail!!, viewModel.newEmail.value!!)
                    } else {
                        currentEmailEdit.error = getString(R.string.error_invalid_email)
                        viewModel.preserveEditEmailState()
                    }
                }
            }
        })

        viewModel.translations.observe(this, Observer {
            binding.translation = it
            errorEmailNoChange = it.errorEmailNoChange
            errorWrongSecurityCode = it.errorWrongSecurity
            (activity as AppCompatActivity).supportActionBar?.title = it.checkEmailScreenTitle
        })

        viewModel.validateSecurityCode.observe(this, Observer {
            Log.i("CheckEmailFragment", "${prefs.securityCode}")
            if (it == prefs.securityCode) { //trebuie comparat cu pref.securityCode
                val credentials = LoginRequestModel(prefs.userEmail ?: "", prefs.password ?: "")
                viewModel.login(credentials)
            } else {
                val positiveButtonClick = { _: DialogInterface, _: Int -> }
                if (errorWrongSecurityCode != null)
                    errorAlert(
                        activity as AppCompatActivity,
                        errorWrongSecurityCode!!,
                        false,
                        positiveButtonClick
                    )
                else
                    errorAlert(
                        activity as AppCompatActivity,
                        getString(R.string.wrong_security_code),
                        false,
                        positiveButtonClick
                    )
            }
        })

        viewModel.status.observe(this, Observer {
            binding.editEmailButton.isEnabled = true
            binding.continueButton.isEnabled = true
            binding.resendEmailButton.isEnabled = true
            when (it.status) {
                WandrApiStatus.LOADING -> {
                    when (it.requestId) {
                        WandrApiRequestId.LOGIN -> {
                            binding.editEmailButton.isEnabled = false
                            binding.resendEmailButton.isEnabled = false
                        }
                        WandrApiRequestId.GET_SECURITY_CODE -> {
                            binding.editEmailButton.isEnabled = false
                            binding.continueButton.isEnabled = false
                        }
                        WandrApiRequestId.UPDATE_EMAIL -> {
                            binding.continueButton.isEnabled = false
                            binding.resendEmailButton.isEnabled = false
                        }
                    }
                    binding.progressBarCheckEmail.visibility = View.VISIBLE
                }
                WandrApiStatus.DONE -> {
                    if (it.requestId == WandrApiRequestId.UPDATE_EMAIL) {
                        prefs.userEmail = viewModel.newEmail.value
                    }
                    binding.progressBarCheckEmail.visibility = View.GONE
                }
                WandrApiStatus.ERROR -> {
                    binding.progressBarCheckEmail.visibility = View.GONE
                    Log.i("LogInFragment", "ERROR")
                }
                //else -> binding.progressBarCheckEmail.visibility = View.GONE
            }
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

        viewModel.newSecurityCode.observe(this, Observer {
            if (null != it)
                prefs.securityCode = it.securityCode.toInt()
        })


        viewModel.error.observe(this, Observer {
            val positiveButtonClick = { _: DialogInterface, _: Int -> }
            if (it.contains("code") && it.contains("message")) {
                if (it.get("code") == 422) {
                    if (errorEmailNoChange != null)
                        errorAlert(
                            activity as AppCompatActivity,
                            errorEmailNoChange!!,
                            false,
                            positiveButtonClick
                        )
                    else
                        errorAlert(
                            activity as AppCompatActivity,
                            getString(R.string.error_email_no_change),
                            false,
                            positiveButtonClick
                        )
                } else {
                    if (DEBUG_MODE)
                        errorAlert(
                            activity as AppCompatActivity,
                            it.get("message").toString(),
                            false,
                            positiveButtonClick
                        )
                }

            }
        })
    }

}
