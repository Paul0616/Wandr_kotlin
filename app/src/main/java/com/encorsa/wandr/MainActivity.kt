package com.encorsa.wandr

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.encorsa.wandr.database.CategoryDatabaseModel
import com.encorsa.wandr.databinding.ActivityMainBinding
import com.encorsa.wandr.mainFragments.main.DrawerFragment
import com.encorsa.wandr.mainFragments.main.MainFragment
import com.encorsa.wandr.models.CategoryModel
import com.encorsa.wandr.utils.Prefs
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), DrawerFragment.FragmentDrawerListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        drawerLayout = binding.drawerLayout
        val navController = this.findNavController(R.id.nav_host_fragment_main)
        setSupportActionBar(mainToolbar)
        NavigationUI.setupWithNavController(mainToolbar, navController, drawerLayout)
//         NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)

        navController.addOnDestinationChangedListener { nc: NavController, nd: NavDestination, _ ->
            if (nd.id == nc.graph.startDestination) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment_main)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    override fun onDrawerItemSelected(item: CategoryDatabaseModel) {
        val prefs = Prefs(applicationContext)
        drawerLayout.closeDrawers()
        prefs.currentCategoryId = item.id
        prefs.currentCategoryName = item.name
        //supportActionBar?.title = item.name
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.main_option_menu, menu)
//        return true
//    }

}
