package com.conversify.data.remote

import com.conversify.data.remote.models.ApiResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ConversifyApi {
    @POST("user/regEmailOrPhone")
    @FormUrlEncoded
    fun registerEmailOrPhoneNumber(@Field("email") email: String? = null,
                                   @Field("countryCode") countryCode: String? = null,
                                   @Field("phoneNumber") phoneNumber: String? = null): Call<ApiResponse<Any>>
}