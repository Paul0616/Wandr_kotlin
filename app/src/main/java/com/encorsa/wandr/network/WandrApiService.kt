package com.encorsa.wandr.network

import com.encorsa.wandr.database.LanguageDatabase
import com.encorsa.wandr.network.models.*
import com.encorsa.wandr.utils.BASE_URL

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*



enum class WandrApiStatus { LOADING, ERROR, DONE }
enum class WandrApiRequestId { GET_LANGUAGES, GET_LABELS, LOGIN, REGISTER, GET_HTML_PAGES }

data class CallAndStatus(
    val status: WandrApiStatus,
    val requestId: WandrApiRequestId
)


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
            Deferred<LanguagesList>
    @GET("LabelApi")
    fun getLabels():
            Deferred<LabelsList>
    @POST("Account/login")
    fun login(@Body body: LoginRequestModel):
            Deferred<LoginResponseModel>
    @GET("HtmlPagesAPI/GetHtmlPagesFiltered")
    fun getHtmlPages(@QueryMap options: HashMap<String, String>):
            Deferred<HtmlPagesList>
    @POST("Account/register")
    fun register(@Body body: RegistrationRequestModel):
            Deferred<SecurityCode>
}

object WandrApi {
    val RETROFIT_SERVICE: WandrApiService by lazy {
        retrofit.create(WandrApiService::class.java)
    }
}