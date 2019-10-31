package com.encorsa.wandr.logInFragments.checkEmail

import android.graphics.Color
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.encorsa.wandr.R
import com.encorsa.wandr.databinding.FragmentCheckEmailBinding
import com.encorsa.wandr.utils.Prefs


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
        // TODO: Use the ViewModel
        binding.checkEmailViewModel = viewModel

        binding.currentEmailEdit.setVisibility(View.GONE)
        binding.currentEmail.text = prefs.userEmail

        viewModel.emailMustBeEdited.observe(this, Observer {
            binding.apply {
                editEmailButton.isEnabled = it!!
                if (it) {
                    currentEmail.visibility = View.GONE
                    currentEmailEdit.setText(currentEmail.text)
//                    uddateEmailButton.setBackgroundColor(
//                        ContextCompat.getColor(
//                            activity!!.applicationContext,
//                            R.color.colorPrimaryLight
//                        )
//                    )
                    currentEmailEdit.visibility = View.VISIBLE
                } else {
                    currentEmail.setText(currentEmailEdit.text.toString())
//                    uddateEmailButton.text = getString(R.string.modify_email_button_text)
//                    uddateEmailButton.setBackgroundColor(
//                        ContextCompat.getColor(
//                            activity!!.applicationContext,
//                            R.color.colorPrimary
//                        )
//                    )
                    currentEmailEdit.visibility = View.GONE
                    currentEmail.visibility = View.VISIBLE
                }
            }
        })
    }

}
