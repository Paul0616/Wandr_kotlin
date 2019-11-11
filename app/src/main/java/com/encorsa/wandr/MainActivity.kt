package com.encorsa.wandr

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
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

class MainActivity : AppCompatActivity(), DrawerFragment.FragmentDrawerListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var binding: ActivityMainBinding
//    private lateinit var drawerToggle: ActionBarDrawerToggle
//    private lateinit var newTitle: CharSequence

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

//        val drawerTitle = title
//        newTitle = title
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

//        drawerToggle = object : ActionBarDrawerToggle(
//            this, /* Host Activity */
//            drawerLayout, /* DrawerLayout object */
//            R.string.drawer_open, /* "Open drawer" description for accessibility */
//            R.string.drawer_close  /* "Close drawer" description for accessibility */
//        ) {
//
//            override fun onDrawerClosed(drawerView: View) {
//                actionBar?.title = newTitle
//                invalidateOptionsMenu() // Creates call to onPrepareOptionsMenu().
//            }
//
//            override fun onDrawerOpened(drawerView: View) {
//                actionBar?.title = drawerTitle
//                invalidateOptionsMenu() // Creates call to onPrepareOptionsMenu().
//            }
//        }
    }

//    override fun setTitle(title: CharSequence?) {
//
//        newTitle = title ?: ""
//        actionBar?.title = title
//    }

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

//    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
//        super.onPostCreate(savedInstanceState, persistentState)
//        drawerToggle.syncState()
//    }
//
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        drawerToggle.onConfigurationChanged(newConfig)
//    }
}
