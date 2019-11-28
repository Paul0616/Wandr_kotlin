package com.encorsa.wandr.logInFragments.viewPrivacy

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
import com.encorsa.wandr.R
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.databinding.FragmentViewPrivacyBinding

import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.utils.DEBUG_MODE


class ViewPrivacyFragment : Fragment() {

    companion object {
        fun newInstance() = ViewPrivacyFragment()
    }

    private lateinit var viewModel: ViewPrivacyViewModel
    private lateinit var binding: FragmentViewPrivacyBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = FragmentViewPrivacyBinding.inflate(inflater)
        binding.setLifecycleOwner(this)
        (activity as AppCompatActivity).supportActionBar?.show()

        binding.acceptCheckBox.setOnClickListener {
            if ((it as CheckBox).isChecked)
                this.findNavController().navigate(ViewPrivacyFragmentDirections.actionViewUrlFragmentToRegisterFragment())
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
            when (it) {
                WandrApiStatus.LOADING -> {
                    binding.progressBarHtmlPage.visibility = View.VISIBLE
                    binding.acceptContainerLayout.visibility = View.GONE
                }
                WandrApiStatus.DONE -> {
                    binding.progressBarHtmlPage.visibility = View.INVISIBLE
                    binding.acceptContainerLayout.visibility = View.VISIBLE
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


        viewModel.acceptPrivacyMessage.observe(this, Observer {
            binding.acceptCheckBox.text = it ?: getString(R.string.accept_privacy)
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

        viewModel.htmlPage.observe(this, Observer {
            if (!it.htmlPagesDescriptions.isEmpty()) {
                (activity as? AppCompatActivity)?.supportActionBar?.title  = it.htmlPagesDescriptions.single().title
                binding.privacyText.text = HtmlCompat.fromHtml(
                    it.htmlPagesDescriptions.get(0).html!!,
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
                binding.privacyText.movementMethod = LinkMovementMethod.getInstance()
            }
        })

    }


}