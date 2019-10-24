package com.encorsa.wandr.splashScreen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.encorsa.wandr.database.LabelDatabase
import com.encorsa.wandr.database.LanguageDatabase
import com.encorsa.wandr.database.WandrDatabaseDao

import com.encorsa.wandr.network.WandrApi
import com.encorsa.wandr.network.WandrApiStatus
import com.encorsa.wandr.network.models.LabelModel
import com.encorsa.wandr.utils.Prefs
import kotlinx.coroutines.*
import retrofit2.HttpException

class SplashScreenViewModel(val database: WandrDatabaseDao) : ViewModel() {

    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val ioScope = CoroutineScope(Dispatchers.IO + viewModelJob)
   // private lateinit var prefs: Prefs


    //API status of the most recent request
    private val _isLogged = MutableLiveData<Boolean>()
    val isLogged: LiveData<Boolean>
        get() = _isLogged

    private val _status = MutableLiveData<WandrApiStatus>()
    val status: LiveData<WandrApiStatus>
        get() = _status

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    //List of languages
    private val _languages = MutableLiveData<List<LanguageDatabase>>()
    val languages: LiveData<List<LanguageDatabase>>
        get() = _languages

    //List of labels
    private val _labels = MutableLiveData<List<LabelModel>>()
    val labels: LiveData<List<LabelModel>>
        get() = _labels


    init {
        Log.i("SplashScrrenViewModel", "CREATED")
        getLanguages()
        getLabels()
      //  prefs = Prefs(app.applicationContext)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        Log.i("SplashScrrenViewModel", "DESTROYED")
    }

    fun errorWasDisplayed(){
        _error.value = null
    }

    private fun getLanguages() {
        coroutineScope.launch {
            // Get the Deferred object for our Retrofit request
            var getLanguagesDeferred = WandrApi.RETROFIT_SERVICE.getLanguages()

            // Await the completion of our Retrofit request
            try {
                _status.value = WandrApiStatus.LOADING
                val listResult = getLanguagesDeferred.await()
                _status.value = WandrApiStatus.DONE
                _languages.value = listResult
                synchronizeLanguages(database, listResult)
                //getLabels()
            }
            catch (e: Exception) {
                _status.value = WandrApiStatus.ERROR
                _error.value = e.message
                //_status.value = "Failure: ${e.message}"
            }
            catch (ex: HttpException){
                _error.value = ex.response().message() + ex.response().errorBody()?.string()
            }
        }
    }

    private fun getLabels() {
        coroutineScope.launch {
            // Get the Deferred object for our Retrofit request
            var getLabelsDeferred = WandrApi.RETROFIT_SERVICE.getLabels()

            // Await the completion of our Retrofit request
            try {
                _status.value = WandrApiStatus.LOADING
                val listResult = getLabelsDeferred.await()
                _status.value = WandrApiStatus.DONE
                _labels.value = listResult
                synchronizeLabels(database, listResult)
            }
            catch (e: Exception) {
                _status.value = WandrApiStatus.ERROR
                _error.value = e.message
                //_status.value = "Failure: ${e.message}"
            }
            catch (ex: HttpException){
                _error.value = ex.response().message() + ex.response().errorBody()?.string()
            }
        }
    }


    fun synchronizeLanguages(wandrDao: WandrDatabaseDao, apiLanguageDatabases: List<LanguageDatabase>) {
        ioScope.launch {
            var inserted: Int = 0
            var updated: Int = 0
            var deleted: Int = 0
            for (languageDatabase: LanguageDatabase in apiLanguageDatabases) {
                val foundedDatabaseLang = wandrDao.findLanguageByLanguageId(languageDatabase.languageId)
                if (null == foundedDatabaseLang) {
                    wandrDao.insertLanguage(languageDatabase)
                    inserted++
                } else {
                    if (languageDatabase.name != foundedDatabaseLang.name || languageDatabase.tag != foundedDatabaseLang.tag) {
                        wandrDao.updateLanguageByRow(
                            foundedDatabaseLang.rowId,
                            languageDatabase.tag,
                            languageDatabase.name
                        )
                        updated++;
                    }
                }
            }
            val allDatabaseLanguages = wandrDao.getAllLanguages()
            for (langDb in allDatabaseLanguages) {
                var wasFoundInApiLanguage = false
                for (langApi in apiLanguageDatabases) {
                    if (langApi.languageId == langDb.languageId) {
                        wasFoundInApiLanguage = true
                        break
                    }
                }
                if (!wasFoundInApiLanguage) {
                    wandrDao.deleteLanguageByRow(langDb.rowId)
                    deleted++;
                }
            }
            withContext(Dispatchers.Main) {
                _error.value =
                    "%s Inserted, %s Updated, %s Deleted".format(inserted, updated, deleted)
            }
        }
    }

    fun synchronizeLabels(wandrDao: WandrDatabaseDao, apiLabels: List<LabelModel>) {
        ioScope.launch {
            var inserted: Int = 0
            var updated: Int = 0
            var deleted: Int = 0
            for (labelModel in apiLabels) {
                for (languageAndNameModel in labelModel.labelNames) {
                    val label =
                        wandrDao.findLabelByLabelId(labelModel.id, languageAndNameModel.language)
                    if (null == label) {
                        val newLabel = LabelDatabase(
                            0,
                            labelModel.tag,
                            languageAndNameModel.name,
                            languageAndNameModel.language,
                            labelModel.id
                        )
                        wandrDao.insertLabel(newLabel)
                        inserted++
                    } else {
                        if(labelModel.tag != label.tag || languageAndNameModel.name != label.name) {
                            wandrDao.updateLabelByRow(
                                label.rowId,
                                labelModel.tag,
                                languageAndNameModel.name!!
                            )
                            updated++
                        }
                    }
                }
            }
            val allLabels = wandrDao.getAllLabels()
            for (label in allLabels) {
                var wasFound = false
                for (labelModel in apiLabels) {
                    for (languageAndNameModel in labelModel.labelNames) {
                        if (label.labelId == labelModel.id && label.languageTag == languageAndNameModel.language) {
                            wasFound = true
                            break
                        }
                    }
                    if (wasFound) {
                        break
                    }
                }
                if (!wasFound) {
                    wandrDao.deleteLabelByRow(label.rowId)
                    deleted++
                }
            }
            withContext(Dispatchers.Main) {
                _error.value =
                    "%s Inserted, %s Updated, %s Deleted".format(inserted, updated, deleted)

            }
        }
    }
}