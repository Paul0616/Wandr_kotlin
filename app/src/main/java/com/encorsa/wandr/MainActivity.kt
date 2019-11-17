package com.encorsa.wandr


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.encorsa.wandr.database.CategoryDatabaseModel
import com.encorsa.wandr.mainFragments.main.DrawerFragment
import com.encorsa.wandr.utils.Prefs


class MainActivity : AppCompatActivity() , DrawerFragment.FragmentDrawerListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onDrawerItemSelected(item: CategoryDatabaseModel) {
        val prefs = Prefs(applicationContext)
        prefs.currentCategoryId = item.id
        prefs.currentCategoryName = item.name
    }

}
