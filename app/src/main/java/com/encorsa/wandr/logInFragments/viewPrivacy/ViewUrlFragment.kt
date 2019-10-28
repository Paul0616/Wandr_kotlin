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
import androidx.lifecycle.Transformations
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController

import com.encorsa.wandr.R
import com.encorsa.wandr.databinding.FragmentViewUrlBinding
import com.encorsa.wandr.network.WandrApiStatus


class ViewUrlFragment : Fragment() {

    companion object {
        fun newInstance() = ViewUrlFragment()
    }

    private lateinit var viewModel: ViewUrlViewModel
    private lateinit var binding: FragmentViewUrlBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = FragmentViewUrlBinding.inflate(inflater)
        val viewModel =
            ViewModelProviders.of(this).get(ViewUrlViewModel::class.java)
        binding.setLifecycleOwner(this)
        (activity as AppCompatActivity).supportActionBar?.show()

        binding.acceptCheckBox.setOnClickListener {
            if ((it as CheckBox).isChecked)
                this.findNavController().navigate(ViewUrlFragmentDirections.actionViewUrlFragmentToRegisterFragment())
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val application = requireNotNull(activity).application
        viewModel = ViewModelProviders.of(this).get(ViewUrlViewModel::class.java)
        // TODO: Use the ViewModel

        binding.viewUrlViewModel = viewModel
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


        viewModel.error.observe(this, Observer {
            Toast.makeText(application.applicationContext,
                when (it) {
                    (null) -> ""
                    else -> it
                },
                Toast.LENGTH_LONG).show()
        })

        viewModel.htmlPage.observe(this, Observer {
            if (!it.htmlPagesDescriptions.isEmpty()) {
                binding.privacyText.text = HtmlCompat.fromHtml(
                    it.htmlPagesDescriptions.get(0).html!!,
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
                binding.privacyText.movementMethod = LinkMovementMethod.getInstance()
            }
        })

    }



}
