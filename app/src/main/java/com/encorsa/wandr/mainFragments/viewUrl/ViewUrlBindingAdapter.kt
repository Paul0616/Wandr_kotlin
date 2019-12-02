package com.encorsa.wandr.mainFragments.viewUrl

import android.util.Log
import android.webkit.WebView
import androidx.databinding.BindingAdapter

@BindingAdapter("url")
fun bindUrl(view: WebView, url: String?) {
    var properUrl: String? = url
    if (!url!!.startsWith("http://") && !url.startsWith("https://")){
        properUrl = "http://$url"
    }
    view.loadUrl(properUrl)
    Log.i("ViewUrlBindingAdapter", "$properUrl")
}