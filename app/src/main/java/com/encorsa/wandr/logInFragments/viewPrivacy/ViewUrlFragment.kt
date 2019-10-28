package com.encorsa.wandr.logInFragments.viewPrivacy

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer

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

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ViewUrlViewModel::class.java)
        // TODO: Use the ViewModel
        viewModel.status.observe(this, Observer {
            when (it) {
                WandrApiStatus.LOADING -> binding.progressBarHtmlPage.visibility = View.VISIBLE
                WandrApiStatus.DONE -> {
                    binding.progressBarHtmlPage.visibility = View.INVISIBLE

                }
                WandrApiStatus.ERROR -> {
                    binding.progressBarHtmlPage.visibility = View.INVISIBLE
                    Log.i("LogInFragment", "ERROR")
                }
                else -> binding.progressBarHtmlPage.visibility = View.INVISIBLE
            }
        })


        viewModel.error.observe(this, Observer {
            binding.privacyText.text = when (it) {
                (null) -> ""
                else -> it
            }
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
