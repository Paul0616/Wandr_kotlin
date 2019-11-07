package com.encorsa.wandr.mainFragments.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager

import com.encorsa.wandr.R
import com.encorsa.wandr.adapters.DrawerAdapter
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.databinding.FragmentDrawerBinding
import com.encorsa.wandr.models.CategoryModel
import com.encorsa.wandr.utils.DEBUG_MODE

class DrawerFragment : Fragment() {

    companion object {
        fun newInstance() = DrawerFragment()
    }

    private lateinit var viewModel: DrawerViewModel
    private lateinit var binding: FragmentDrawerBinding
    private lateinit var adapter: DrawerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDrawerBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val application = requireNotNull(activity).application
        val dataSource = WandrDatabase.getInstance(application).wandrDatabaseDao
        val viewModelFactory = DrawerModelFactory(application, dataSource)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(DrawerViewModel::class.java)

        val menuItems = ArrayList<CategoryModel>()
        adapter = DrawerAdapter(menuItems)


        binding.rvDrawerList.layoutManager = LinearLayoutManager(activity)
        viewModel.menuItems.observe(this, Observer {
            adapter = DrawerAdapter(it)
            binding.rvDrawerList.adapter = adapter

        })

        viewModel.networkErrors.observe(this, Observer {
            if (DEBUG_MODE)
                Toast.makeText(
                    application.applicationContext,
                    when (it) {
                        (null) -> ""
                        else -> it
                    },
                    Toast.LENGTH_LONG
                ).show()
        })
    }

}
