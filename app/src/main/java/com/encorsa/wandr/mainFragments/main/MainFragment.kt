package com.encorsa.wandr.mainFragments.main


import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.encorsa.wandr.LogInActivity

import com.encorsa.wandr.R
import com.encorsa.wandr.adapters.ObjectiveAdapter
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.databinding.FragmentMainBinding
import com.encorsa.wandr.utils.DEBUG_MODE
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

        val application = requireNotNull(activity).application
        val dataSource = WandrDatabase.getInstance(application).wandrDatabaseDao
        val binding = FragmentMainBinding.inflate(inflater)
        val viewModelFactory = MainModelFactory(application, dataSource)
        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)

        val adapter = ObjectiveAdapter()
        setHasOptionsMenu(true)
        setupScrollListener(binding.objectiveList, viewModel)


        binding.setLifecycleOwner(this)
        binding.mainViewmodel = viewModel
        binding.progressBarMain.visibility = View.GONE
        viewModel.currentLanguage.observe(this, Observer {
            it?.let {
                viewModel.loadObjectives()
            }
        })

        binding.testButton.setOnClickListener (
            Navigation.createNavigateOnClickListener(MainFragmentDirections.actionMainFragmentToDetailFragment())
        )
        binding
        adapter.initAdapter(binding, viewModel, application)
        return binding.root
    }

    private fun ObjectiveAdapter.initAdapter(
        binding: FragmentMainBinding,
        viewModel: MainViewModel,
        application: Application
    ) {
        binding.objectiveList.adapter = this
        viewModel.objectives.observe(this@MainFragment, Observer {
            showEmptyList(it?.size == 0, binding.emptyList, binding.objectiveList)
            this.submitList(it)
        })

        viewModel.networkErrors.observe(this@MainFragment, Observer {
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

    private fun setupScrollListener(list: RecyclerView, viewModel: MainViewModel) {
        val layoutManager = list.layoutManager as LinearLayoutManager
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = layoutManager.itemCount
                val visibleItemCount = layoutManager.childCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                viewModel.objectiveListScrolled(visibleItemCount, lastVisibleItem, totalItemCount)
            }
        })
    }

    private fun showEmptyList(show: Boolean, view: TextView, list: RecyclerView) {
        if (show) {
            view.visibility = View.VISIBLE
            list.visibility = View.GONE
        } else {
            view.visibility = View.GONE
            list.visibility = View.VISIBLE
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_option_menu, menu)


//        val searchView = menu.findItem(R.id.app_bar_search).actionView as SearchView
////        val closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn)
////
////        closeButton.setOnClickListener(View.OnClickListener {
////            if (searchView.query.toString() == "") {
//////                search = false
//////                loadedPages = 0
////            }
////            searchView.isIconified = true
////            //attemptGetObjectives()
////        })
//        searchView.setOnCloseListener(object: SearchView.OnCloseListener{
//            override fun onClose(): Boolean {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//        })
//
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(s: String): Boolean {
//                searchView.setQuery("", false)
//                searchView.isIconified = true
//
////                search = true
////                searchString = s
////                loadedPages = 0
////                setSearchViewText(searchView)
////                attemptGetObjectives()
//                return true
//            }
//
//            override fun onQueryTextChange(s: String): Boolean {
//                return false
//            }
//
//        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logOut) {
            val prefs = Prefs(requireNotNull(activity).applicationContext)
            prefs.logOut()
            startActivity(Intent(activity, LogInActivity::class.java))
            (activity as AppCompatActivity).finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
