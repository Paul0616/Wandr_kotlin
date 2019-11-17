package com.encorsa.wandr.mainFragments.main


import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.encorsa.wandr.LogInActivity
import com.encorsa.wandr.MapsActivity

import com.encorsa.wandr.R
import com.encorsa.wandr.adapters.ObjectiveAdapter
import com.encorsa.wandr.database.SubcategoryDatabaseModel
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.databinding.FragmentMainBinding

import com.encorsa.wandr.utils.*
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.fragment_detail.view.toolbar

/**
 * A simple [Fragment] subclass.
 */
class MainFragment : Fragment() {
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val application = requireNotNull(activity).application
        val dataSource = WandrDatabase.getInstance(application).wandrDatabaseDao

        binding = FragmentMainBinding.inflate(inflater)
        val viewModelFactory = MainModelFactory(application, dataSource)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)

        //(activity as AppCompatActivity).setSupportActionBar(binding.root.toolbar)
        (activity as AppCompatActivity).supportActionBar?.show()
//        (activity as AppCompatActivity).supportActionBar?.setHomeButtonEnabled(true)
//        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val adapter = ObjectiveAdapter(
            application.applicationContext,
            ObjectiveAdapter.OnClickListener { obj, favoriteWasClicked, insertMode ->
                if (!favoriteWasClicked)
                    viewModel.objectiveWasClicked(obj)
                else {

                    viewModel.favoriteWasClicked(obj, insertMode)
                }
            })

        setHasOptionsMenu(true)
        setupScrollListener(binding.objectiveList, viewModel)
        binding.setLifecycleOwner(this)
        binding.mainViewmodel = viewModel
        binding.progressBarMain.visibility = View.GONE
        //(activity as AppCompatActivity).supportActionBar?.show()

        /*  --------------------------
         *   observe if shared preference changes
         *  ----------------------------
         */
        val prefsLiveData = PreferenceManager.getDefaultSharedPreferences(context)
        prefsLiveData.stringLiveData(CURRENT_LANGUAGE, null).observe(this, Observer {
            Log.i("MainFragment", it ?: "")
            (activity as? AppCompatActivity)?.supportActionBar?.title =
                prefsLiveData.getString(CURRENT_CATEGORY_NAME, "")
        })

        prefsLiveData.stringLiveData(CURRENT_CATEGORY_ID, null).observe(this, Observer {
            Log.i("MainFragment", "LIVE CURRENT CATEG")
            viewModel.makeQueryForCategoryId(it)
            viewModel.setCurrentCategory(it)
        })

        prefsLiveData.stringLiveData(CURRENT_CATEGORY_NAME, null).observe(this, Observer {
            (activity as? AppCompatActivity)?.supportActionBar?.title =
                prefsLiveData.getString(CURRENT_CATEGORY_NAME, "")
        })


        /*  --------------------------
         *   observe onclick on media image
         *  ----------------------------
         */
        viewModel.navigateToDetails.observe(this, Observer {
            if(null != it) {
                Log.i("TESTMainViewModel", "OBJECTIVE ${it.id}")
                this.findNavController().navigate(MainFragmentDirections.actionMainFragmentToDetailFragment(it))
                viewModel.displayDetailsComplete()
            }


//            startActivity(Intent(activity, MapsActivity::class.java))
        })

        /*  --------------------------
         *   observe subcategories list and refresh UI
         *   (old CHIPS will be removed and new ones will be created and added)
         *  ----------------------------
         */
        viewModel.subcategoriesList.observe(
            viewLifecycleOwner,
            object : Observer<List<SubcategoryDatabaseModel>> {
                override fun onChanged(t: List<SubcategoryDatabaseModel>?) {
                    t ?: return
                    val chipGroup = binding.subcategoriesList
                    val inflator = LayoutInflater.from(chipGroup.context)

                    val children = t.map { subcategory ->
                        val chip = inflator.inflate(R.layout.subcategory, chipGroup, false) as Chip
                        chip.text = subcategory.name
                        chip.tag = subcategory.id
                        chip.setOnCheckedChangeListener { chipButton, _ ->
                            viewModel.getSelectedChipsTags(chipButton)
                        }
                        chip
                    }
                    chipGroup.removeAllViews()
                    for (chip in children) {
                        chipGroup.addView(chip)
                    }

                }
            })

        binding.expandSubcategoriesLayout.setOnClickListener {
            viewModel.changeChipsGroupVisibility()
        }

        viewModel.chipsGroupIsVisible.observe(this, Observer {
            binding.viewFilterIcon.isSelected = it
            when (it) {
                true -> binding.subcategoriesList.visibility = View.VISIBLE
                false -> binding.subcategoriesList.visibility = View.GONE
            }
        })

        viewModel.subcategoryFilterApplied.observe(this, Observer {
            when (it) {
                true -> binding.filterOnImageView.visibility = View.VISIBLE
                false -> binding.filterOnImageView.visibility = View.INVISIBLE
            }

        })

        /*  --------------------------
         *   observe errors from subcategories repository
         *   and from set favorites
         *  ----------------------------
         */
        viewModel.networkErrorsSubcategories.observe(this, Observer {
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

        /*  --------------------------
         *   refresh UI on added favorite
         *  ----------------------------
         */
        viewModel.favoriteId.observe(this, Observer {
            viewModel.refreshWithCurrentFilter()
        })

        adapter.initAdapter(application)
        return binding.root
    }


    /*  --------------------------
     *   initiate adapter and set
     *   - media observer
     *   - error from media repository observer
     *  ----------------------------
     */
    private fun ObjectiveAdapter.initAdapter(
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

    /*  --------------------------
    *   function for setting scroll listener of recycler view
    *   - treating scrolling in viewModel
    *  ----------------------------
    */
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

    /*  --------------------------
    *   establish if empty list message need to be shown
    *  ----------------------------
    */
    private fun showEmptyList(show: Boolean, view: TextView, list: RecyclerView) {
        if (show) {
            view.visibility = View.VISIBLE
            list.visibility = View.GONE
        } else {
            view.visibility = View.GONE
            list.visibility = View.VISIBLE
        }
    }

    /*  --------------------------
     *  listener for searchView from menu bar
     *  ----------------------------
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_option_menu, menu)

        val searchView = menu.findItem(R.id.app_bar_search).actionView as SearchView

        val closeButton =
            searchView.findViewById(androidx.appcompat.R.id.search_close_btn) as ImageView

        closeButton.setOnClickListener(View.OnClickListener {
            searchView.setQuery("", false)
            searchView.isIconified = true
            viewModel.setSearch(null)
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                viewModel.setSearch(s)
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(s: String): Boolean {
                return false
            }
        })
    }

    /*  --------------------------
     *  option menu
     *  - Log OUT
     *  - Change LANGUAGE
     *  ----------------------------
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logOut) {
            val prefs = Prefs(requireNotNull(activity).applicationContext)
            prefs.logOut()
            startActivity(Intent(activity, LogInActivity::class.java))
            (activity as AppCompatActivity).finish()
        }
        if (item.itemId == R.id.action_language) {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToLanguageSettingsFragment())
        }
        return super.onOptionsItemSelected(item)
    }


}
