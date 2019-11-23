package com.encorsa.wandr.mainFragments.imageSlider

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ImageSliderViewModel : ViewModel() {

    private var _leftArrowIsVisible = MutableLiveData<Boolean>(false)
    val leftArrowIsVisible: LiveData<Boolean>
        get() = _leftArrowIsVisible

    private var _rightArrowIsVisible = MutableLiveData<Boolean>(false)
    val rightArrowIsVisible: LiveData<Boolean>
        get() = _rightArrowIsVisible

    init {

    }

    fun pageWasChanged(position: Int, mediaCount: Int) {
        Log.i("ImageSliderViewModel", "position ${position} - mediaCount ${mediaCount}")
        _rightArrowIsVisible.value = position !=  mediaCount - 1
        _leftArrowIsVisible.value = position != 0
    }

    override fun onCleared() {
        super.onCleared()
    }
}