package com.encorsa.wandr.network

import com.encorsa.wandr.models.*
import com.encorsa.wandr.utils.BASE_URL

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*



enum class WandrApiStatus { LOADING, ERROR, DONE }
enum class WandrApiRequestId { GET_SECURITY_CODE, UPDATE_EMAIL, LOGIN, REGISTER}

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
    fun login(@Body body: LoginRequestModel, @Query("confirmEmail") confirmEmail: Boolean):
            Deferred<LoginResponseModel>
    @GET("HtmlPagesAPI/GetHtmlPagesFiltered")
    fun getHtmlPages(@QueryMap options: HashMap<String, String>):
            Deferred<HtmlPagesList>
    @POST("Account/register")
    fun register(@Body body: RegistrationRequestModel):
            Deferred<SecurityCode>
    @PUT("Account/security-code")
    fun getNewSecurityCode(@Query("email") email: String, @Body body: SecurityCode):
            Deferred<SecurityCode>
    @PUT("Account/update-email")
    fun updateEmail(@Query("oldEmail") oldEmail: String, @Query("newEmail") newEmail: String):
            Deferred<Response<Unit>>
    @GET("ObjectiveApi/GetObjectivesFiltered")
    fun getObjectives(@QueryMap options: HashMap<String, Any>, @Query("subcategoryId") subcategoryIdList: List<String>?):
            Deferred<ObjectivePage>
    @GET("CategoryAPI")
    fun getCategories(@QueryMap options: HashMap<String, String>):
            Deferred<CategoryList>
    @GET("SubcategoryApi")
    fun getSubcategories(@QueryMap options: HashMap<String, String>):
            Deferred<SubcategoryList>
    @POST("FavoriteApi")
    fun addFavorite(@Body body: FavoriteInsertModel,@Header("Authorization") token: String, @Header("Content-Type") contentType: String):
            Deferred<FavoriteIdModel>
    @DELETE("FavoriteApi/{id}")
    fun removeFavorite(@Path("id") id: String, @Header("Authorization") token: String):
            Deferred<FavoriteIdModel>
    @GET("ObjectiveApi/{id}")
    fun getObjectiveById(@Path("id") id: String, @QueryMap options: HashMap<String, Any>):
            Deferred<ObjectiveModel>
}

object WandrApi {
    val RETROFIT_SERVICE: WandrApiService by lazy {
        retrofit.create(WandrApiService::class.java)
    }
}

