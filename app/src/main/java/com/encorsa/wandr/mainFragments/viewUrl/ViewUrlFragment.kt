package com.encorsa.wandr.mainFragments.viewUrl

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.encorsa.wandr.databinding.FragmentViewUrlBinding
import com.encorsa.wandr.utils.makeTransperantStatusBar

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
        binding.lifecycleOwner = this
        (activity as AppCompatActivity).setSupportActionBar(binding.urlToolbar)
        activity?.window?.let {
            makeTransperantStatusBar(activity?.window!!, false)
        }
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val objective = ViewUrlFragmentArgs.fromBundle(arguments!!).objective
        binding.webView.webViewClient = myWebClient()
        binding.objective = objective
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ViewUrlViewModel::class.java)


        binding.urlToolbar.setNavigationOnClickListener {
            Log.i("ViewUrlFragment", "navigationup")
            //findNavController().navigate(VideoPlayerFragmentDirections.actionVideoPlayerFragmentToDetailFragment(objective))
            findNavController().navigateUp()
        }
    }

    inner class myWebClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

            binding.urlProgressBar.visibility = View.VISIBLE
            view.loadUrl(url)
            return true

        }

        override fun onPageFinished(view: WebView, url: String) {

            super.onPageFinished(view, url)
            binding.urlProgressBar.visibility = View.GONE
        }

    }

}
