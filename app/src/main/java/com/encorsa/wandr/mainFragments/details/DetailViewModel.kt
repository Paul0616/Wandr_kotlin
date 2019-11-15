package com.encorsa.wandr.mainFragments.details

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
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

    init {
        Log.i("DetailViewModel", "CREATED")
        _selectedObjective.value = objective
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

    val imageNumber:LiveData<Int> =
        Transformations.map(media) {
           it.filter {
                it.mediaType == "image"
            }.size
        }

    val videoNumber =
        Transformations.map(media) {
            it.filter {
                it.mediaType == "video"
            }.size
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


}
