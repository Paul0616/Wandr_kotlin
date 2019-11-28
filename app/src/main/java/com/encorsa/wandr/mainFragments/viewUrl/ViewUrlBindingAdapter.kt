package com.encorsa.wandr.mainFragments.viewUrl

import android.util.Log
import android.webkit.WebView
import androidx.databinding.BindingAdapter

@BindingAdapter("url")
fun bindUrl(view: WebView, url: String?) {
    if (!url!!.startsWith("http://") && !url.startsWith("https://")){
        view.loadUrl("http://$url")
    } else
        view.loadUrl(url)

    Log.i("ViewUrlBindingAdapter", "$url")
}