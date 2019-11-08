package com.encorsa.wandr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.encorsa.wandr.databinding.ActivityMainBinding
import com.encorsa.wandr.mainFragments.main.DrawerFragment
import com.encorsa.wandr.mainFragments.main.MainFragment
import com.encorsa.wandr.models.CategoryModel
import com.encorsa.wandr.utils.Prefs

class MainActivity : AppCompatActivity(), DrawerFragment.FragmentDrawerListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        drawerLayout = binding.drawerLayout
        val navController = this.findNavController(R.id.nav_host_fragment_main)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)

        navController.addOnDestinationChangedListener { nc: NavController, nd: NavDestination, arguments ->
            if (nd.id == nc.graph.startDestination) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }
        //NavigationUI.setupWithNavController(binding.navView, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment_main)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    override fun onDrawerItemSelected(item: CategoryModel) {
        val prefs = Prefs(applicationContext)
        drawerLayout.closeDrawers()
        prefs.currentCategoryId = item.id
    }
}
