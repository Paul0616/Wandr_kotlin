package com.encorsa.wandr.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Utilities {


    fun getLongDate(dateString: String?): Long? {
        val test = try {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            formatter.parse(dateString).time

        } catch (e: ParseException) {
            null
        }
        return test
    }

    fun setLanguageConfig(ctx: Context) {
        val prefs = Prefs(ctx)
        var currentLanguage = prefs.currentLanguage.let {
            it ?: Locale.getDefault().language
        }

        val locale = Locale(currentLanguage)
        Locale.setDefault(locale)
        val config = Configuration()

        @SuppressWarnings("deprecation")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
        } else {
            config.locale = locale
        }
        ctx.resources.updateConfiguration(
            config,
            ctx.resources.displayMetrics
        )

    }
}