package com.encorsa.wandr.mainFragments.videoPlayer

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.encorsa.wandr.database.WandrDatabaseDao

class VideoPlayerModelFactory(private val application: Application,
                              private val dataSource: WandrDatabaseDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VideoPlayerViewModel::class.java)) {
            return VideoPlayerViewModel(application, dataSource) as T
        }
        throw java.lang.IllegalArgumentException("Unknown ViewModel class")
    }
}