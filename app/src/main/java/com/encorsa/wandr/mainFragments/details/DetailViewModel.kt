package com.encorsa.wandr.mainFragments.details

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.encorsa.wandr.database.ListMediaDatabaseModel
import com.encorsa.wandr.database.MediaDatabaseModel
import com.encorsa.wandr.database.ObjectiveDatabaseModel
import com.encorsa.wandr.database.WandrDatabaseDao
import com.encorsa.wandr.models.*
import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.repository.ObjectivesRepository
import com.encorsa.wandr.utils.Prefs
import com.encorsa.wandr.utils.Utilities
import kotlinx.coroutines.*
import retrofit2.HttpException

enum class FabStatus { FAVORITE, NOT_FAVORITE}
class DetailViewModel(
    app: Application,
    private val database: WandrDatabaseDao,
    private val objective: ObjectiveDatabaseModel
) :
    AndroidViewModel(app) {

    private val objectiveRepository = ObjectivesRepository(app, database)
    private val prefs = Prefs(app.applicationContext)

    private var viewModelJob = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + viewModelJob)
    private val objectiveId = MutableLiveData<String?>()

    init {
        Log.i("DetailViewModel", "CREATED WITH id = ${objective.id}")
        objectiveId.postValue(objective.id)
    }

    /* ---------------------------------------------------------------------
        -observe init of objectiveId LiveData and it change get Objective
        from Repository
        -changing of selectedObjectives list will trigger selectedObjective
        object change
        -changing selectedObjective trigger fabStatus
        -binding from fragment_detail layout and DetailBindingAdapter will
        update the UI

    * ----------------------------------------------------------------------
    */


    private var selectedObjectives: LiveData<List<ObjectiveDatabaseModel>> =
        Transformations.switchMap(objectiveId) {
            objectiveRepository.getRepositoryObjectiveById(it!!)
        }


    var selectedObjective: LiveData<ObjectiveDatabaseModel> = Transformations.switchMap(selectedObjectives){
        MutableLiveData<ObjectiveDatabaseModel>(it[0])
    }

    var fabStatus: LiveData<FabStatus> = Transformations.switchMap(selectedObjective) {
        when (it.isFavorite) {
            true -> MutableLiveData<FabStatus>(FabStatus.FAVORITE)
            false -> MutableLiveData<FabStatus>(FabStatus.NOT_FAVORITE)
        }
    }


    /* ---------------------------------------------------------------------
        -changing selectedObjective will be observed in DetailFragment and
        getMedia will be called. This func will get media for objectiveId
        from repository
        -result will trigger mediaRepositoryResponse and then media and/or
        networkerror will be initiate
        -media change will be filtered and photoGallery LiveData list and
        videoGallery LiveData list will be created. These values will be
        observed in DetailFragment and correspondent buttond will be displayed

    * ----------------------------------------------------------------------
    */

    private val _mediaRepositoryResponse = MutableLiveData<MediaRepositoryResult>()
    private val mediaRepositoryResponse: LiveData<MediaRepositoryResult>
        get() = _mediaRepositoryResponse

    fun getMedia() {
        viewModelScope.launch {
            Log.i("DetailViewModel", "GET MEDIA")
            _mediaRepositoryResponse.value =
                objectiveRepository.getRepositoryMedia(objective.id)
        }
    }

    var networkErrors: LiveData<String> =
        Transformations.switchMap(mediaRepositoryResponse) { it ->
            it.networkErrors
        }

    private var media: LiveData<List<MediaDatabaseModel>> =
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

    /* ---------------------------------------------------------------------
       -if photo or video gallery buttons will be clicked functions will
       initiate displayPhotoGallery respectivelly videoPhotoGallery that will
       trigger navigation
   * ----------------------------------------------------------------------
   */
    private val _displayPhotoGallery = MutableLiveData<ListMediaDatabaseModel>()
    val displayPhotoGallery: LiveData<ListMediaDatabaseModel>
        get() = _displayPhotoGallery

    private val _displayVideoGallery = MutableLiveData<ListMediaDatabaseModel>()
    val displayVideoGallery: LiveData<ListMediaDatabaseModel>
        get() = _displayVideoGallery

    fun showPhotoGallery() {
        val medias = ListMediaDatabaseModel()
        photoGallery.value?.let {
            medias.addAll(photoGallery.value!!)
        }
        _displayPhotoGallery.value = medias
    }

    fun showVideoGallery() {
        val medias = ListMediaDatabaseModel()
        videoGallery.value?.let {
            medias.addAll(videoGallery.value!!)
        }
        _displayVideoGallery.value = medias
    }


    override fun onCleared() {
        super.onCleared()
        Log.i("DetailViewModel", "DESTROYED")
        viewModelJob.cancel()
    }

    /* ---------------------------------------------------------------------
     - add/delete favorite from FAB clicking
     -after adding or deleting favoriteId will be changed and can be observed
     in DetailFragment and it call refresh database from network
     * ----------------------------------------------------------------------
     */
    fun favoriteWasClicked(objective: ObjectiveDatabaseModel) {
        val shouldAddToFavorite = !objective.isFavorite
        Log.i(
            "DetailViewModel",
            "ADD TO FAVORITE: ID:${objective.id}"
        )
        shouldAddToFavorite.let {
            if (it) {
                val favoriteForInsert = FavoriteInsertModel(prefs.userId!!, objective.id)
                addTofavorite(favoriteForInsert)
            } else {
                deleteFromFavorite(objective.favoriteId)
            }
        }
    }

    /* ---------------------------------------------------
        *  first check if token is expired and if is make network login call
        *  make network delete favorite call
        *  capturing errors in error LiveData
        *  if call was succesfull favoriteId will change and in MainFragment call refresh screen
        * ---------------------------------------------------
        */
    private fun deleteFromFavorite(favoriteId: String?) {
        ioScope.launch {
            val time = System.currentTimeMillis()
            var err: String? = null
            var favoriteIdModel: FavoriteIdModel? = null
            try {
                if (prefs.tokenExpireAtInMillis < time) {
                    val credentials = LoginRequestModel(prefs.userEmail!!, prefs.password!!)
                    val getTokenModel = WandrApi.RETROFIT_SERVICE.login(credentials, false)
                    val tokenModel = getTokenModel.await()
                    prefs.userEmail = tokenModel.email
                    prefs.userId = tokenModel.userId
                    prefs.userName = tokenModel.userName
                    prefs.token = tokenModel.token
                    prefs.firstName = tokenModel.firstName
                    val tokenExpireAt = Utilities.getLongDate(tokenModel.tokenExpirationDate)
                    if (null != tokenExpireAt)
                        prefs.tokenExpireAtInMillis = tokenExpireAt
                }
                val token = "Bearer ${prefs.token}"
                val defferedIdModel =
                    WandrApi.RETROFIT_SERVICE.removeFavorite(favoriteId!!, token)
                favoriteIdModel = defferedIdModel.await()
            } catch (e: Exception) {
                err = e.message!!
            } catch (ex: HttpException) {
                err = ex.response().message() + ex.response().errorBody()?.string()
            }
            withContext(Dispatchers.Main) {
                err?.let {
                    _error.value = err
                }

                favoriteIdModel?.let {
                    Log.i("TEST", favoriteIdModel.id)
                    _favoriteId.value = favoriteIdModel.id
                }

            }
        }
    }

    /* ---------------------------------------------------
    *  first check if token is expired and if it is, make network login call
    *  make network insert favorite call
    *  capturing errors in error LiveData
    *  if call was succesfull favoriteId will change and in MainFragment call refresh screen
    * ---------------------------------------------------
    */
    private fun addTofavorite(favorite: FavoriteInsertModel) {
        ioScope.launch {
            val time = System.currentTimeMillis()
            var err: String? = null
            var favoriteIdModel: FavoriteIdModel? = null
            try {
                if (prefs.tokenExpireAtInMillis < time) {
                    val credentials = LoginRequestModel(prefs.userEmail!!, prefs.password!!)
                    val getTokenModel = WandrApi.RETROFIT_SERVICE.login(credentials, false)
                    val tokenModel = getTokenModel.await()
                    prefs.userEmail = tokenModel.email
                    prefs.userId = tokenModel.userId
                    prefs.userName = tokenModel.userName
                    prefs.token = tokenModel.token
                    prefs.firstName = tokenModel.firstName
                    val tokenExpireAt = Utilities.getLongDate(tokenModel.tokenExpirationDate)
                    if (null != tokenExpireAt)
                        prefs.tokenExpireAtInMillis = tokenExpireAt
                }
                val token = "Bearer ${prefs.token}"
                val defferedIdModel =
                    WandrApi.RETROFIT_SERVICE.addFavorite(favorite, token, "application/json")
                favoriteIdModel = defferedIdModel.await()
            } catch (e: Exception) {
                err = e.message!!
            } catch (ex: HttpException) {
                err = ex.response().message() + ex.response().errorBody()?.string()
            }
            withContext(Dispatchers.Main) {
                err?.let {
                    _error.value = err
                }

                favoriteIdModel?.let {
                    Log.i("TEST", favoriteIdModel.id)
                    _favoriteId.value = favoriteIdModel.id
                }

            }
        }
    }

    private val _favoriteId = MutableLiveData<String>()
    val favoriteId: LiveData<String>
        get() = _favoriteId

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    fun refreshObjective(id: String) {
        viewModelScope.launch {
            objectiveRepository.makeNetworkCallAndRefreshObjective(
                id,
                prefs.currentLanguage!!,
                prefs.userId!!
            )
            selectedObjectives = objectiveRepository.getRepositoryObjectiveById(id)
        }
    }



}
