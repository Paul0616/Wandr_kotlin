package com.encorsa.wandr.logInFragments.viewPrivacy

import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer

import androidx.navigation.fragment.findNavController
import com.encorsa.wandr.MainActivity
import com.encorsa.wandr.R
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.databinding.FragmentViewPrivacyBinding
import com.encorsa.wandr.models.LoginRequestModel
import com.encorsa.wandr.models.RegistrationRequestModel
import com.encorsa.wandr.network.WandrApiRequestId

import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.utils.DEBUG_MODE
import com.encorsa.wandr.utils.Prefs
import com.encorsa.wandr.utils.Utilities


class ViewPrivacyFragment : Fragment() {

    companion object {
        fun newInstance() = ViewPrivacyFragment()
    }

    private lateinit var viewModel: ViewPrivacyViewModel
    private lateinit var binding: FragmentViewPrivacyBinding
    var googleUser:RegistrationRequestModel? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = FragmentViewPrivacyBinding.inflate(inflater)
        binding.setLifecycleOwner(this)
        (activity as AppCompatActivity).supportActionBar?.show()
        val withGoogle = ViewPrivacyFragmentArgs.fromBundle(arguments!!).withGoogle
        googleUser = ViewPrivacyFragmentArgs.fromBundle(arguments!!).newGoogleUser
        binding.acceptCheckBox.setOnClickListener {
            if ((it as CheckBox).isChecked){
                if (!withGoogle)
                    this.findNavController().navigate(ViewPrivacyFragmentDirections.actionViewUrlFragmentToRegisterFragment())
                else
                    viewModel.registerNewGoogleUser(googleUser!!)
            }

        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val application = requireNotNull(activity).application
        val dataSource = WandrDatabase.getInstance(application).wandrDatabaseDao
        val viewModelFactory = ViewPrivacyModelFactory(application, dataSource)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ViewPrivacyViewModel::class.java)


        binding.viewPrivacyViewModel = viewModel
        viewModel.status.observe(this, Observer {
            when (it.status) {
                WandrApiStatus.LOADING -> {
                    binding.progressBarHtmlPage.visibility = View.VISIBLE
                    binding.acceptContainerLayout.visibility = View.GONE
                }
                WandrApiStatus.DONE -> {
                    binding.progressBarHtmlPage.visibility = View.INVISIBLE
                    binding.acceptContainerLayout.visibility = View.VISIBLE
                    if(it.requestId == WandrApiRequestId.REGISTER){
                        val credentials = LoginRequestModel(googleUser?.email!!, googleUser?.password!!)
                        viewModel.login(credentials)
                    }
                }
                WandrApiStatus.ERROR -> {
                    binding.progressBarHtmlPage.visibility = View.INVISIBLE
                    binding.acceptContainerLayout.visibility = View.GONE
                    Log.i("LogInFragment", "ERROR")
                }
                else -> {
                    binding.progressBarHtmlPage.visibility = View.INVISIBLE
                    binding.acceptContainerLayout.visibility = View.GONE
                }
            }
        })


        viewModel.translations.observe(this, Observer {
            binding.acceptCheckBox.text = it.acceptTerms
            (activity as AppCompatActivity).supportActionBar?.title = it.screenTitle
            Log.i("TEST", it.screenTitle)
        })

        viewModel.error.observe(this, Observer {
            if (DEBUG_MODE) {
                if (it.contains("message")){
                    val errorMess = it.get("message") as String?
                    Toast.makeText(
                        application.applicationContext,
                        when (errorMess) {
                            (null) -> ""
                            else -> errorMess
                        },
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })

        viewModel.tokenModel.observe(this, Observer {
            val prefs = Prefs(requireNotNull(activity).applicationContext)
            prefs.userEmail = it?.email
            prefs.userId = it?.userId
            prefs.userName = it?.userName
            prefs.token = it?.token
            prefs.firstName = it?.firstName
            prefs.password = googleUser?.password

            val tokenExpireAt = Utilities.getLongDate(it?.tokenExpirationDate)
            if (null != tokenExpireAt)
                prefs.tokenExpireAtInMillis = tokenExpireAt
            startActivity(Intent(activity, MainActivity::class.java))
            (activity as AppCompatActivity).finish()
        })

        viewModel.htmlPage.observe(this, Observer {
            if (!it.htmlPagesDescriptions.isEmpty()) {
                //(activity as? AppCompatActivity)?.supportActionBar?.title  = it.htmlPagesDescriptions.single().title
                binding.privacyText.text = HtmlCompat.fromHtml(
                    it.htmlPagesDescriptions.get(0).html!!,
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
                binding.privacyText.movementMethod = LinkMovementMethod.getInstance()
            }
        })

    }


}
