package com.encorsa.wandr.mainFragments.videoPlayer

import android.widget.ImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("isSelected")
fun bindButtonSelected(view: ImageView, isSelected: Boolean) {
    view.isSelected = isSelected
}