package com.encorsa.wandr.mainFragments.main

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.encorsa.wandr.R

@BindingAdapter("shortDescriptionFromHtmlString")
fun setShortDescription(view: TextView, longDescription: String?){
    longDescription?.let {
        view.text = fromHtml(longDescription).toString()
    }
}

@SuppressWarnings("deprecation")
fun fromHtml(html: String): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(html)
    }
}

@BindingAdapter("textString")
fun setText(view: TextView, txt: String?){
    txt?.let {
        view.text = txt
    }
}

@BindingAdapter("selectedFavorite")
fun setSelectedFavorite(view: ImageView, isSelected: Boolean){
    view.isSelected = isSelected
}

@BindingAdapter("defaultUrl")
fun setImageFromUrl(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        Glide.with(view.context)
            .load(imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .centerCrop()
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_no_image)
            )
            .into(view)
    }
}


