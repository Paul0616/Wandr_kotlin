package com.encorsa.wandr.mainFragments.main


import android.app.AlertDialog
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.encorsa.wandr.LogInActivity

import com.encorsa.wandr.R
import com.encorsa.wandr.adapters.ObjectiveAdapter
import com.encorsa.wandr.database.ObjectiveDatabaseModel
import com.encorsa.wandr.database.SubcategoryDatabaseModel
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.databinding.FragmentMainBinding

import com.encorsa.wandr.utils.*
import com.google.android.material.chip.Chip

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
        var messageForNoDetails: String? = getString(R.string.no_info)
        var location1: String = getString(R.string.location_message_1)
        var location2: String = getString(R.string.location_message_2)

        binding = FragmentMainBinding.inflate(inflater)
        val viewModelFactory = MainModelFactory(application, dataSource)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)

        (activity as AppCompatActivity).setSupportActionBar(binding.mainToolbar)
        activity?.window?.let {
            makeTransperantStatusBar(activity?.window!!, false)
        }

        val adapter = ObjectiveAdapter(
            application.applicationContext,
            ObjectiveAdapter.OnClickListener { obj, viewClicked ->
                viewModel.objectiveWasClicked(obj, viewClicked)
            })

        setHasOptionsMenu(true)
        setupScrollListener(binding.objectiveList, viewModel)
        binding.setLifecycleOwner(this)
        binding.mainViewmodel = viewModel
        binding.progressBarMain.visibility = View.GONE


        val navController = findNavController()
        NavigationUI.setupWithNavController(
            binding.mainToolbar,
            navController,
            binding.drawerLayout
        )

        /*  --------------------------
         *   observe if shared preference changes
         *  ----------------------------
         */
        val prefsLiveData = PreferenceManager.getDefaultSharedPreferences(context)
        prefsLiveData.stringLiveData(CURRENT_LANGUAGE, null).observe(this, Observer {
            Log.i("MainFragment", it ?: "")
            (activity as? AppCompatActivity)?.supportActionBar?.title =
                prefsLiveData.getString(CURRENT_CATEGORY_NAME, "")
            viewModel.setCurrentLanguage(it)
        })

        prefsLiveData.stringLiveData(CURRENT_CATEGORY_ID, null).observe(this, Observer {
            Log.i("MainFragment", "LIVE CURRENT CATEG")
            viewModel.makeQueryForCategoryId(it)
            viewModel.setCurrentCategory(it)
        })

        prefsLiveData.stringLiveData(CURRENT_CATEGORY_NAME, null).observe(this, Observer {
            (activity as? AppCompatActivity)?.supportActionBar?.title =
                prefsLiveData.getString(CURRENT_CATEGORY_NAME, "")
            binding.drawerLayout.closeDrawers()
        })

        /*  --------------------------
         *   observe language change
         *  ----------------------------
         */
        viewModel.currentLanguage.observe(this, Observer {
            viewModel.getLabelByTagAndLanguage(it)
        })

        viewModel.translationsMain.observe(this, Observer{
            ObjectiveAdapter.ItemViewHolder.translationsMain = it
            messageForNoDetails = it.noInfo
            it.locationWhithGoogle?.let {loc ->
                location1 = loc
            }
            it.locationWhithWaze?.let {loc ->
                location2 = loc
            }
            binding.translation = it
        })

        /*  --------------------------
         *   observe NAVIGATION
         *  ----------------------------
         */
        viewModel.navigateToDetails.observe(this, Observer {
            if (null != it) {
                Log.i("MainFragment", "OBJECTIVE ${it.id}")
                if (it.containUsefulInfo()) {
                    this.findNavController()
                        .navigate(MainFragmentDirections.actionMainFragmentToDetailFragment(it))
                    viewModel.displayDetailsComplete()
                } else {
                    Toast.makeText(context, messageForNoDetails ?: "Nu există alte detalii", Toast.LENGTH_SHORT).show()
                }
            }
        })


        viewModel.navigateToMap.observe(this, Observer {
            if (null != it) {
                showLocationTypeDialog(it, arrayOf(
                    location1,
                    location2
                ))
            }
        })

        viewModel.navigateToUrl.observe(this, Observer {
            if (null != it) {
                Log.i("MainFragment", "OBJECTIVE ${it.id}")
                this.findNavController().navigate(MainFragmentDirections.actionMainFragmentToViewUrlFragment4(it))
                viewModel.navigateToUrlComplete()
            }
        })

        viewModel.navigateToSettings.observe(this, Observer {
            if (false != it) {
               // startActivity(Intent(activity, SettingsActivity::class.java))
                 this.findNavController()
                                    .navigate(MainFragmentDirections.actionMainFragmentToSettingsActivity())
                viewModel.navigateToSettingsComplete()
            }
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
       *   function alert dialog with options list
       *   for choosing how to view objective LOCATION
       *  ----------------------------
       */

    private fun showLocationTypeDialog(objective: ObjectiveDatabaseModel, array: Array<String>){
        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(context)
        builder.setSingleChoiceItems(array, -1) { dialogInterface, which ->
            when(which){
                0 -> {
                    Log.i("MainFragment", "OBJECTIVE ${objective.id}")
                    this.findNavController()
                        .navigate(MainFragmentDirections.actionMainFragmentToMapsActivity(objective))
                    viewModel.navigateToMapComplete()
                }
                1 -> {
                    try {
                        var url = "waze://?ll="
                        url = url.plus(objective.latitude)
                        url = url.plus(",")
                        url = url.plus(objective.longitude)
                        url = url.plus("&navigate=yes")
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: ActivityNotFoundException){
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze")))
                    }
                }
            }
            dialog.dismiss()
        }

        dialog = builder.create()
        dialog.show()
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
        return when (item.itemId) {
            R.id.logOut -> {
                val prefs = Prefs(requireNotNull(activity).applicationContext)
                prefs.logOut()
                startActivity(Intent(activity, LogInActivity::class.java))
                (activity as AppCompatActivity).finish()
                true
            }
            R.id.action_language -> {
                viewModel.navigateToSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}
