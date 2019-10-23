package com.encorsa.wandr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.database.LanguageDatabase
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.databinding.ActivitySplashScreenBinding
import com.encorsa.wandr.network.models.LabelModel
import com.encorsa.wandr.splashScreen.SplashScreenViewModel
import com.encorsa.wandr.splashScreen.SplashScreenViewModelFactory

class SplashScreenActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivitySplashScreenBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_splash_screen)
        //val dataSource = WandrDatabase.getInstance(application).wandrDatabaseDao
        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.setLifecycleOwner(this)

        val dataSource = WandrDatabase.getInstance(application).wandrDatabaseDao
        val viewModelFactory =
            SplashScreenViewModelFactory(dataSource)
        val viewModel: SplashScreenViewModel = ViewModelProviders.of(this, viewModelFactory).get(
            SplashScreenViewModel::class.java)

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
                else ->  binding.progressBar.visibility = View.INVISIBLE
            }
        })

        viewModel.languages.observe(this, Observer
        {
            for (languageDatabase: LanguageDatabase in it) {
                Log.i("SplashScreenActivity", languageDatabase.name + " - " + languageDatabase.rowId)
            }
        })

        viewModel.labels.observe(this, Observer
        {
            for (label: LabelModel in it) {
                Log.i("SplashScreenActivity", label.tag + " - " + label.labelNames.size)
            }
            startActivity(Intent(this, LogInActivity::class.java))
        })

        viewModel.error.observe(this, Observer {
            if(null != it){
                Log.i("SplashScreenActivity", it)
                viewModel.errorWasDisplayed()
            }
        })
    }
}
