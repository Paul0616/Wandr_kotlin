package com.encorsa.wandr

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.*
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.mainFragments.settings.LanguageSettingsViewModel
import com.encorsa.wandr.mainFragments.settings.LanguageSettingsViewModelFactory
import com.encorsa.wandr.utils.CURRENT_LANGUAGE
import com.encorsa.wandr.utils.Prefs

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private lateinit var viewModel: LanguageSettingsViewModel
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val prefs = Prefs(context!!)
            val application = requireNotNull(activity).application
            val dataSource = WandrDatabase.getInstance(application).wandrDatabaseDao
            val viewModelFactory = LanguageSettingsViewModelFactory(application, dataSource)
            viewModel = ViewModelProviders.of(this, viewModelFactory).get(LanguageSettingsViewModel::class.java)


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
//                for(lang in list){
//                    if (lang.tag == prefs.currentLanguage){
//                        listPreference?.setSummary(lang.name)
//                        listPreference?.value = lang.name
//                        break
//                    }
//                }

//                PreferenceManager.setDefaultValues(context, CURRENT_LANGUAGE, Context.MODE_PRIVATE, R.xml.root_preferences, false);
            })
        }
    }
}