package com.encorsa.wandr.logInFragments.viewPrivacy

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.encorsa.wandr.R
import com.encorsa.wandr.databinding.FragmentViewUrlBinding


class ViewUrlFragment : Fragment() {

    companion object {
        fun newInstance() = ViewUrlFragment()
    }

    private lateinit var viewModel: ViewUrlViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val binding = FragmentViewUrlBinding.inflate(inflater)
        val viewModel =
            ViewModelProviders.of(this).get(ViewUrlViewModel::class.java)
        binding.setLifecycleOwner(this)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ViewUrlViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
