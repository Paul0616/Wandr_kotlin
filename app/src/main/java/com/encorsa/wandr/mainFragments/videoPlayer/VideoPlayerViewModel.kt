package com.encorsa.wandr.mainFragments.videoPlayer

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.encorsa.wandr.R
import com.encorsa.wandr.database.MediaDatabaseModel
import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.utils.TranslationsVideoPlayer
import kotlinx.coroutines.*

class VideoPlayerViewModel(app: Application, val database: WandrDatabaseDao) :
    AndroidViewModel(app) {
    // TODO: Implement the ViewModel
    private var viewModelJob = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + viewModelJob)
    init {
        Log.i("VideoPlayerViewModel", "CREATED")

    }

    private val _currentLanguage = MutableLiveData<String>()
    val currentLanguage: LiveData<String>
        get() = _currentLanguage

    private val _translations = MutableLiveData<TranslationsVideoPlayer>(
        TranslationsVideoPlayer(
            app.getString(R.string.you_tube_connection_error)
        )
    )
    val translations: LiveData<TranslationsVideoPlayer>
        get() = _translations

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

    /*  -----------------------------------
   *   LANGUAGE CHANGE:
   *   - get labels for current tag language
   *   - set new language
   *  ------------------------------------
   */
    fun getLabelByTagAndLanguage(languageTag: String) {
        ioScope.launch {
            val youTubFailure = database.findlabelByTag("you_tube_connection_error", languageTag)
            withContext(Dispatchers.Main) {
                _translations.value = TranslationsVideoPlayer(
                    youTubeConnectionerror = youTubFailure?.name
                )
                Log.i(
                    "TRANSLATIONS1",
                    "${_translations.value?.youTubeConnectionerror}"
                )
            }
        }
    }

    fun setCurrentLanguage(language: String?) {
        _currentLanguage.value = language
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("VideoPlayerViewModel", "DESTROYED")
        viewModelJob.cancel()
    }
}
