package com.encorsa.wandr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI

class LogInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        val navcon = this.findNavController(R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navcon)
        //supportActionBar?.hide()
    }

    //    override fun onNavigateUp(): Boolean {
//        val navcon = this.findNavController(R.id.nav_host_fragment)
//        return navcon.navigateUp()
//    }
    override fun onSupportNavigateUp(): Boolean {
        val navcon = this.findNavController(R.id.nav_host_fragment)
        return navcon.navigateUp()
    }
}
