package com.encorsa.wandr.mainFragments.main


import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.encorsa.wandr.LogInActivity
import com.encorsa.wandr.MainActivity

import com.encorsa.wandr.R
import com.encorsa.wandr.databinding.FragmentMainBinding
import com.encorsa.wandr.utils.Prefs

/**
 * A simple [Fragment] subclass.
 */
class MainFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment


        val binding = FragmentMainBinding.inflate(inflater)
        val viewModel = ViewModelProviders.of(this).get(MainFragmentModel::class.java)
        setHasOptionsMenu(true)

        binding.testButton.setOnClickListener (
            Navigation.createNavigateOnClickListener(MainFragmentDirections.actionMainFragmentToDetailFragment())
        )

        return binding.root
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_option_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val prefs = Prefs(requireNotNull(activity).applicationContext)
        prefs.logOut()
        startActivity(Intent(activity, LogInActivity::class.java))
        return super.onOptionsItemSelected(item)
    }
}
