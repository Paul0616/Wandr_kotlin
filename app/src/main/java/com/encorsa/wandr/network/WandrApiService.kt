package com.encorsa.wandr.network

import com.encorsa.wandr.database.LanguageDatabase
import com.encorsa.wandr.network.models.LabelModel

import com.encorsa.wandr.network.models.LoginRequestModel
import com.encorsa.wandr.network.models.LoginResponseModel
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = "https://harta-ar-interactiva.azurewebsites.net/api/"

enum class WandrApiStatus { LOADING, ERROR, DONE }

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()


private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

interface WandrApiService {
    @GET("LanguageAPI")
    fun getLanguages():
            Deferred<List<LanguageDatabase>>
    @GET("LabelApi")
    fun getLabels():
            Deferred<List<LabelModel>>
    @POST("Account/login")
    fun login(@Body body: LoginRequestModel):
            Deferred<LoginResponseModel>
}

object WandrApi {
    val RETROFIT_SERVICE: WandrApiService by lazy {
        retrofit.create(WandrApiService::class.java)
    }
}