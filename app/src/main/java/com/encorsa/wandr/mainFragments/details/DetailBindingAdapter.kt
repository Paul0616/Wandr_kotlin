package com.encorsa.wandr.mainFragments.details

import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.encorsa.wandr.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

@BindingAdapter("imageFromUrl")
fun bindImageFromUrl(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        Glide.with(view.context)
            .load(imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(view)
    }
}

@BindingAdapter("renderHtml")
fun bindRenderHtml(view: TextView, description: String?) {
    if (description != null) {
        view.text = HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_COMPACT)
        view.movementMethod = LinkMovementMethod.getInstance()
    } else {
        view.text = ""
    }
}

@BindingAdapter("setFavorite")
fun bindSetVavorite(view: FloatingActionButton, fabStatus: FabStatus?) {
    Log.i("DetailBindingAdapter", "${fabStatus}")
    fabStatus?.let {
        when (fabStatus) {
            FabStatus.FAVORITE -> {
                Log.i("DetailBindingAdapter", "FAB FAVORITE")
                view.setImageResource(R.drawable.ic_favorite_orange_24dp)
                view.isEnabled = true
            }
            FabStatus.NOT_FAVORITE ->  {
                Log.i("DetailBindingAdapter", "FAB NOT FAVORITE")
                view.setImageResource(R.drawable.ic_favorite_border_orange_24dp)
                view.isEnabled = true
            }
        }
    }
}