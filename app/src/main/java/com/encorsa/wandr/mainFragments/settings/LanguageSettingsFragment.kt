package com.encorsa.wandr.mainFragments.settings

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations

import androidx.preference.PreferenceFragmentCompat
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory

import com.encorsa.wandr.R
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.utils.CURRENT_LANGUAGE
import com.encorsa.wandr.utils.Prefs
import com.encorsa.wandr.utils.makeTransperantStatusBar


class LanguageSettingsFragment : PreferenceFragmentCompat() {

    private lateinit var viewModel: LanguageSettingsViewModel

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.widgets_preferences, rootKey)

        val prefs = Prefs(context!!)
        val application = requireNotNull(activity).application
        val dataSource = WandrDatabase.getInstance(application).wandrDatabaseDao
        val viewModelFactory = LanguageSettingsViewModelFactory(application, dataSource)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(LanguageSettingsViewModel::class.java)

//        val navcon = findNavController()
//        NavigationUI.setupActionBarWithNavController(, navcon)
//        (activity as AppCompatActivity).supportActionBar?.show()
//        activity?.window?.let {
//            makeTransperantStatusBar(activity?.window!!, false)
//        }

        val prefCat = findPreference<Preference>("preferences_category") as PreferenceCategory?
        val listPreference = findPreference<Preference>("current_language") as ListPreference?
        viewModel.settingsTitle.observe(this, Observer {
            (activity as? AppCompatActivity)?.supportActionBar?.title = it
        })
        viewModel.listPreferenceTitle.observe(this, Observer {
            listPreference?.title = it
        })
        viewModel.preferenceCategoryTitle.observe(this, Observer {
            prefCat?.title = it
        })
        viewModel.languagesList.observe(this, Observer { list ->
            val entries = list.map {
                val entry: CharSequence = it.name
                entry
            }.toTypedArray()
            Log.i("SettingsFragment", entries.toList().toString())
            val entryValues = list.map {
                val entryValue: CharSequence = it.tag
                entryValue
            }.toTypedArray()
            Log.i("SettingsFragment", "${entryValues.toList()} - ${prefs.currentLanguage}")
            listPreference?.entries = entries
            listPreference?.entryValues = entryValues
            //listPreference?.setDefaultValue("RO")
        })
    }



}
