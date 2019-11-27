package com.encorsa.wandr.mainFragments.videoPlayer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.encorsa.wandr.database.MediaDatabaseModel

class VideoPlayerViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    init {
        Log.i("VideoPlayerViewModel", "CREATED")

    }

    private val _videoId = MutableLiveData<String>()
    val videoId: LiveData<String>
        get() = _videoId

    private var _videos = MutableLiveData<List<MediaDatabaseModel>>()
    val videos: LiveData<List<MediaDatabaseModel>>
        get() = _videos

    /* -----------------------
    *  click on video title
    * ------------------------
    */
    fun videoWasClicked(video: MediaDatabaseModel) {
        _videoId.value = video.mediaUrl
        _videos.value = videos.value?.map {
            if (it == video)
                it.isSelected = true
            else
                it.isSelected = false
            it
        }
    }

    /* -----------------------------------------
    *  set videos with currentVideoPos selected
    * ------------------------------------------
    */
    fun setVideos(currentVideoPos: Int, videos: List<MediaDatabaseModel>) {
        _videoId.value = videos[currentVideoPos].mediaUrl
        _videos.value = videos.mapIndexed {index, video ->
            if (index == currentVideoPos)
                video.isSelected = true
            else
                video.isSelected = false
            video
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("VideoPlayerViewModel", "DESTROYED")
    }
}
