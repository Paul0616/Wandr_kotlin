package com.encorsa.wandr.mainFragments.videoPlayer

import android.util.Log
import androidx.lifecycle.ViewModel

class VideoPlayerViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    init{
        Log.i("VideoPlayerViewModel", "CREATED")

    }

    override fun onCleared() {
        super.onCleared()
        Log.i("VideoPlayerViewModel", "DESTROYED")
    }
}
