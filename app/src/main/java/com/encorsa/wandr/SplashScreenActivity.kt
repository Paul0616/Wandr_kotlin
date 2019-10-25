package com.encorsa.wandr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.database.LanguageDatabase
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.databinding.ActivitySplashScreenBinding
import com.encorsa.wandr.network.models.LabelModel
import com.encorsa.wandr.splashScreen.SplashScreenViewModel
import com.encorsa.wandr.splashScreen.SplashScreenViewModelFactory
import com.encorsa.wandr.utils.Prefs

class SplashScreenActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivitySplashScreenBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_splash_screen)
        binding.setLifecycleOwner(this)

        val dataSource = WandrDatabase.getInstance(application).wandrDatabaseDao
        val prefs = Prefs(application.applicationContext)
        val viewModelFactory =
            SplashScreenViewModelFactory(dataSource)
        val viewModel: SplashScreenViewModel = ViewModelProviders.of(this, viewModelFactory).get(
            SplashScreenViewModel::class.java
        )

        viewModel.status.observe(this, Observer {

            when (it) {
                WandrApiStatus.LOADING -> binding.progressBar.visibility = View.VISIBLE
                WandrApiStatus.DONE -> {
                    binding.progressBar.visibility = View.INVISIBLE
                }
                WandrApiStatus.ERROR -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    Log.i("SplashScreenActivity", "ERROR")
                }
                else -> binding.progressBar.visibility = View.INVISIBLE
            }
        })

        viewModel.languages.observe(this, Observer
        {
            for (languageDatabase: LanguageDatabase in it) {
                Log.i(
                    "SplashScreenActivity",
                    languageDatabase.name + " - " + languageDatabase.rowId
                )
            }
        })

        viewModel.error.observe(this, Observer {
            if (null != it) {
                Log.i("SplashScreenActivity", it)
                viewModel.errorWasDisplayed()
            }
        })

        viewModel.labels.observe(this, Observer
        {
            for (label: LabelModel in it) {
                Log.i("SplashScreenActivity", label.tag + " - " + label.labelNames.size)
            }
            Log.i("SplashScreenActivity", prefs.userName ?: "USER NAME NULL")
            if (prefs.userName == null) {
                val i = Intent(this, LogInActivity::class.java)
                //i.putExtra("title","XXX")
                startActivity(i)
                //this.findNavController()
            }
            else
                startActivity(Intent(this, MainActivity::class.java))
        })


    }
}
