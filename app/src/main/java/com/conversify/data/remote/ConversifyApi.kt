package com.conversify.data.remote

import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.loginsignup.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ConversifyApi {
    @POST("user/regEmailOrPhone")
    @FormUrlEncoded
    fun registerEmailOrPhoneNumber(@Field("email") email: String? = null,
                                   @Field("countryCode") countryCode: String? = null,
                                   @Field("phoneNumber") phoneNumber: String? = null): Call<ApiResponse<ProfileDto>>

    @POST("user/logIn")
    fun login(@Body request: LoginRequest): Call<ApiResponse<ProfileDto>>

    @POST("user/verifyOTP")
    fun verifyOtp(@Body request: VerifyOtpRequest): Call<ApiResponse<ProfileDto>>

    @POST("user/resendOTP")
    fun resendOtp(@Body request: ResendOtpRequest): Call<Any>

    @POST("user/signUp")
    fun signUp(@Body request: SignUpRequest): Call<ApiResponse<ProfileDto>>

    @POST("user/getData")
    @FormUrlEncoded
    fun getInterests(@Field("flag") flag: Int = ApiConstants.FLAG_INTERESTS): Call<ApiResponse<List<InterestDto>>>
}