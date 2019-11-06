package com.encorsa.wandr.utils

import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.encorsa.wandr.R
import com.encorsa.wandr.network.models.QueryModel
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


const val BASE_URL = "https://harta-ar-interactiva.azurewebsites.net/api/"
const val DEBUG_MODE = true
const val DEFAULT_LANGUAGE = "RO"
const val URL_PRIVACY = "PRIVACY"
const val PAGE_SIZE = 20


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

    fun errorAlert(context: Context, message: String, hasNegativeButton: Boolean, positiveButtonClick: (DialogInterface, Int) -> Unit) {
       // val positiveButtonClick = { _: DialogInterface, _: Int -> }
        val builder = MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog) //.Builder(context)

        with(builder)
        {
            setTitle(context.getString(R.string.app_name))
            setMessage(message)
            setCancelable(false)
            if (hasNegativeButton)
                setNegativeButton("Cancel", { _: DialogInterface, _: Int -> })
            setPositiveButton("OK", DialogInterface.OnClickListener(positiveButtonClick))
            show()
        }
    }

    fun getQuery(
        queryModel: QueryModel
    ): SupportSQLiteQuery {
        Log.i("Utilities",
            "New query: language:${queryModel.languageTag} favorite: ${queryModel.onlyFavorite?.toString()} name: ${queryModel.name?.toString()} categoryId: ${queryModel.categoryId?.toString()} subcategoryIds ${queryModel.subcategoryIds?.toString()}")
        val bindArgs = ArrayList<Any>()

        var stringSelection = ""
        val builder = SupportSQLiteQueryBuilder
            .builder("objectives_table")
            .columns(arrayOf("*"))

        if (!queryModel.languageTag.isEmpty()) {
            bindArgs.add(queryModel.languageTag)
            stringSelection += " languageTag = ?"
        }

        if (queryModel.onlyFavorite != null) {
            bindArgs.add(queryModel.onlyFavorite!!)
            stringSelection += " AND isFavorite = ?"
        }

        if (queryModel.name != null) {
            bindArgs.add("%".plus(queryModel.name).plus("%"))
            stringSelection += " AND name LIKE ?"
        }

        if (queryModel.categoryId != null) {
            bindArgs.add(queryModel.categoryId!!)
            stringSelection += " AND categoryId = ?"
        }

        if (queryModel.subcategoryIds != null) {
            val str = "?,".repeat(queryModel.subcategoryIds!!.size).dropLast(1)
            bindArgs.addAll(queryModel.subcategoryIds!!)
            stringSelection += " AND subcategoryId IN (${str})"
        }

        if (!stringSelection.equals(""))
            builder.selection(stringSelection, bindArgs.toArray())

        builder.orderBy("name")
        val query = builder.create()

        Log.i("WandrDatabaseTest", query.sql + " " + bindArgs.toString())

        return query
    }
}

fun String.smartTruncate(length: Int): String {
    val PUNCTUATION = listOf(", ", "; ", ": ", " ")
    val words = split(" ")
    var added = 0
    var hasMore = false
    val builder = StringBuilder()
    for (word in words) {
        if (builder.length > length) {
            hasMore = true
            break
        }
        builder.append(word)
        builder.append(" ")
        added += 1
    }

    PUNCTUATION.map {
        if (builder.endsWith(it)) {
            builder.replace(builder.length - it.length, builder.length, "")
        }
    }

    if (hasMore) {
        builder.append("...")
    }
    return builder.toString()
}