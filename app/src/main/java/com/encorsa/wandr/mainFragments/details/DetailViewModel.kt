package com.encorsa.wandr.mainFragments.details

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.encorsa.wandr.database.ListMediaDatabaseModel
import com.encorsa.wandr.database.MediaDatabaseModel
import com.encorsa.wandr.database.ObjectiveDatabaseModel
import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.models.MediaRepositoryResult
import com.encorsa.wandr.repository.ObjectivesRepository
import kotlinx.coroutines.launch

class DetailViewModel(
    app: Application,
    private val database: WandrDatabaseDao,
    private val objective: ObjectiveDatabaseModel
) :
    AndroidViewModel(app) {

    private val objectiveRepository = ObjectivesRepository(app, database)

    private val _selectedObjective = MutableLiveData<ObjectiveDatabaseModel>()
    val selectedObjective: LiveData<ObjectiveDatabaseModel>
        get() = _selectedObjective

    private val _displayPhotoGallery = MutableLiveData<ListMediaDatabaseModel>()
    val displayPhotoGallery: LiveData<ListMediaDatabaseModel>
        get() = _displayPhotoGallery

    private val _displayVideoGallery = MutableLiveData<ListMediaDatabaseModel>()
    val displayVideoGallery: LiveData<ListMediaDatabaseModel>
        get() = _displayVideoGallery

    val objectiveName = MutableLiveData<String?>()


    init {
        Log.i("DetailViewModel", "CREATED")
        _selectedObjective.value = objective
        objectiveName.value = objective.name
        Log.i("DetailViewModel", objective.defaultImageUrl)
        getMedia()
    }


    private val _mediaRepositoryResponse = MutableLiveData<MediaRepositoryResult>()
    private val mediaRepositoryResponse: LiveData<MediaRepositoryResult>
        get() = _mediaRepositoryResponse


    var media: LiveData<List<MediaDatabaseModel>> =
        Transformations.switchMap(mediaRepositoryResponse) { it ->
            it.media
        }

    val photoGallery: LiveData<List<MediaDatabaseModel>> =
        Transformations.map(media) {
            it.filter {
                it.mediaType == "image"
            }
        }

    val videoGallery: LiveData<List<MediaDatabaseModel>> =
        Transformations.map(media) {
            it.filter {
                it.mediaType == "video"
            }
        }

    var networkErrors: LiveData<String> =
        Transformations.switchMap(mediaRepositoryResponse) { it ->
            it.networkErrors
        }

    override fun onCleared() {
        super.onCleared()
        Log.i("DetailViewModel", "DESTROYED")

    }

    private fun getMedia() {
        viewModelScope.launch {
            _mediaRepositoryResponse.value =
                objectiveRepository.getRepositoryMedia(objective.id)
        }
    }

    fun showPhotoGallery(){
        val medias = ListMediaDatabaseModel()
        photoGallery.value?.let {
            medias.addAll(photoGallery.value!!)
        }
        _displayPhotoGallery.value = medias
    }

    fun showVideoGallery(){
        val medias = ListMediaDatabaseModel()
        videoGallery.value?.let {
            medias.addAll(videoGallery.value!!)
        }
        _displayVideoGallery.value = medias
    }

//    private val _viewMenu = MutableLiveData<Boolean>(true)
//    val viewMenu: LiveData<Boolean>
//        get() = _viewMenu
//    fun switchMenu(){
//        Log.i("DetailViewModel", "${_viewMenu.value}")
//        _viewMenu.value = !_viewMenu.value!!
//    }

}
