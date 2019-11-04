package com.encorsa.wandr.mainFragments.main


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.encorsa.wandr.LogInActivity

import com.encorsa.wandr.R
import com.encorsa.wandr.adapters.ObjectiveAdapter
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.databinding.FragmentMainBinding
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.network.models.ObjectiveModel
import com.encorsa.wandr.utils.DEBUG_MODE
import com.encorsa.wandr.utils.PAGE_SIZE
import com.encorsa.wandr.utils.PaginationScrollListener
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
        var isLastPage: Boolean = false
        var isLoading: Boolean = false
        var nextLoadingPage: Int = 0
       // var objectiveItems = List<ObjectiveModel>()

        binding.objectiveList.addOnScrollListener(object : PaginationScrollListener(binding.objectiveList.layoutManager as LinearLayoutManager){
            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

            override fun loadMoreItems() {
                isLoading = true
                val options = HashMap<String, Any>()
                options.put("languageTag", "RO")
                options.put("pageId", nextLoadingPage)
                options.put("pageSize", PAGE_SIZE)
                Log.i("MainFragment", "LOAD MORE ITEMS page ${nextLoadingPage}")
                viewModel.getObjectives(options, null)
            }
        })

        binding.objectiveList.adapter = adapter

        binding.testButton.setOnClickListener (
            Navigation.createNavigateOnClickListener(MainFragmentDirections.actionMainFragmentToDetailFragment())
        )

        viewModel.objectives.observe(this, Observer {
            it?.let {
                adapter.submitList(it.objectives)
                //adapter.addData(it.objectives)
                isLastPage = it.isLastPage()
                nextLoadingPage = it.currentPage() + 1
                Log.i("MainFragment", "isLastPage = ${isLastPage.toString()} nextLoadingPage = ${nextLoadingPage.toString()}")
            }

        })

        viewModel.status.observe(this, Observer {
            Log.i("MainFragment", "Status changed to ${it?.toString()}")
            when (it) {
                WandrApiStatus.LOADING -> binding.progressBarMain.visibility = View.VISIBLE
                WandrApiStatus.DONE -> {
                    isLoading = false
                    binding.progressBarMain.visibility = View.INVISIBLE
                }
                WandrApiStatus.ERROR -> {
                    binding.progressBarMain.visibility = View.INVISIBLE
                    Log.i("MainFragment", "ERROR")
                }
                else -> binding.progressBarMain.visibility = View.INVISIBLE
            }
        })

        viewModel.error.observe(this, Observer {
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
        return binding.root
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_option_menu, menu)

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
