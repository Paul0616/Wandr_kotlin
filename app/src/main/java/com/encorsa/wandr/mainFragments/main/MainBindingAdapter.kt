package com.encorsa.wandr.mainFragments.main

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.encorsa.wandr.R

@BindingAdapter(value = ["shortDescriptionFromHtmlString", "translationShort"], requireAll = false)
fun setShortDescription(view: TextView, longDescription: String?, translation: String?){
    translation?.let {
        view.text = it
    }
    longDescription?.let {
        view.text = fromHtml(longDescription).toString()
    }
}

@BindingAdapter("isVisible")
fun setVisibility(view: View, isVisible: Boolean){
    when(isVisible){
        true -> view.visibility = View.VISIBLE
        false -> view.visibility = View.INVISIBLE
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

@BindingAdapter(value = ["textString", "translation"], requireAll = false)
fun setText(view: TextView, txt: String?, translation: String?){
    translation?.let {
        view.text = it
    }
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
    } else {
        val drawable = ContextCompat.getDrawable(view.context, R.drawable.ic_no_image)
        view.setImageDrawable(drawable)
    }
}

@BindingAdapter("showUrl")
fun setUrlButton(view: ImageView, url: String?){
    if (url != null) {
        view.isSelected = true
        view.isEnabled = true
    }
    else {
        view.isSelected = false
        view.isEnabled = false
    }
}


