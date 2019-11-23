package com.encorsa.wandr.mainFragments.imageSlider

import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("arrowVisibility")
fun bindArrowVisibility(view: ImageView, isVisible: Boolean){
    view.visibility = if (isVisible) {
        View.VISIBLE
    } else {
        View.INVISIBLE
    }
}

