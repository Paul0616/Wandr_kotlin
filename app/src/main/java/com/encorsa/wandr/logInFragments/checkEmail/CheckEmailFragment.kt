package com.encorsa.wandr.logInFragments.checkEmail

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.encorsa.wandr.R

class CheckEmailFragment : Fragment() {

    companion object {
        fun newInstance() = CheckEmailFragment()
    }

    private lateinit var viewModel: CheckEmailViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_check_email, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CheckEmailViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
