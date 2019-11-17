package com.encorsa.wandr.mainFragments.main

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager

import com.encorsa.wandr.R
import com.encorsa.wandr.adapters.DrawerAdapter
import com.encorsa.wandr.database.CategoryDatabaseModel
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.databinding.FragmentDrawerBinding
import com.encorsa.wandr.models.CategoryModel
import com.encorsa.wandr.utils.*

class DrawerFragment : Fragment() {

    companion object {
        fun newInstance() = DrawerFragment()
    }

    private lateinit var viewModel: DrawerViewModel
    private lateinit var binding: FragmentDrawerBinding
    private lateinit var adapter: DrawerAdapter
    private var drawerListener: FragmentDrawerListener? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDrawerBinding.inflate(inflater)
        binding.lifecycleOwner = this
        try {
            drawerListener = context as FragmentDrawerListener
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val application = requireNotNull(activity).application
        val dataSource = WandrDatabase.getInstance(application).wandrDatabaseDao
        val viewModelFactory = DrawerModelFactory(application, dataSource)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(DrawerViewModel::class.java)

        val menuItems = ArrayList<CategoryDatabaseModel>()
        adapter = DrawerAdapter(application.applicationContext, menuItems, DrawerAdapter.OnClickListener {
            viewModel.categoryMenuWasClicked(it)

        })
        val prefs = Prefs(application)
        val prefsLiveData = PreferenceManager.getDefaultSharedPreferences(context)
        prefsLiveData.stringLiveData(CURRENT_LANGUAGE, null).observe(this, Observer {
           // Log.i("DrawerFragment", it ?: "")
            it?.let {
                viewModel.setCurrentLanguage(it)
            }
        })

        binding.rvDrawerList.layoutManager = LinearLayoutManager(activity)
        viewModel.menuItems.observe(this, Observer {
            adapter = DrawerAdapter(application.applicationContext, it, DrawerAdapter.OnClickListener {
                viewModel.categoryMenuWasClicked(it)
            })
            binding.rvDrawerList.adapter = adapter
            if (!it.isEmpty()) {

                if (prefs.currentCategoryId == null) {
                    val defaultCategory = it.filter {
                        it.tag.contains("DEFAULT")
                    }.single()
                    prefs.currentCategoryId = defaultCategory.id
                    prefs.currentCategoryName = defaultCategory.name
                }

                try {
                    val selectedCategory = it.filter {
                        it.id == prefs.currentCategoryId
                    }.single()

                    prefs.currentCategoryName = selectedCategory.name
                } catch (e: Exception) {
                    e.printStackTrace()
                }


            }
        })

        viewModel.currentLanguage.observe(this, Observer {
            viewModel.loadCategories()

        })

        viewModel.selectedCategory.observe(this, Observer {
            drawerListener?.onDrawerItemSelected(it)
            Log.i("DrawerFragment","${it}")
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

    interface FragmentDrawerListener {
        fun onDrawerItemSelected(item: CategoryDatabaseModel)
    }

}
