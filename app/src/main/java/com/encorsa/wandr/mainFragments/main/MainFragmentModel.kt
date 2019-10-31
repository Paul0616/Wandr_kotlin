package com.encorsa.wandr.mainFragments.main


import android.util.Log
import androidx.lifecycle.ViewModel



class MainFragmentModel: ViewModel() {

    init {
        Log.i("MainFragmentModel", "CREATED")
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("MainFragmentModel", "DESTROYED")
    }
}