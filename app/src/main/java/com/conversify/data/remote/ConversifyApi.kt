package com.conversify.data.remote

import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.chat.VenueDetailsResponse
import com.conversify.data.remote.models.groups.CreateEditGroupRequest
import com.conversify.data.remote.models.groups.GetGroupPostsResponse
import com.conversify.data.remote.models.groups.GetGroupsResponse
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.loginsignup.*
import com.conversify.data.remote.models.venues.CreateEditVenueRequest
import com.conversify.data.remote.models.venues.GetVenuesResponse
import com.conversify.data.remote.models.venues.VenueDto
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

    @POST("user/forgotPassword")
    @FormUrlEncoded
    fun resetPassword(@Field("email") email: String): Call<Any>

    @POST("user/verifyOTP")
    fun verifyOtp(@Body request: VerifyOtpRequest): Call<ApiResponse<ProfileDto>>

    @POST("user/resendOTP")
    fun resendOtp(@Body request: ResendOtpRequest): Call<Any>

    @POST("user/signUp")
    fun signUp(@Body request: SignUpRequest): Call<ApiResponse<ProfileDto>>

    @POST("user/userNameCheck")
    @FormUrlEncoded
    fun usernameAvailability(@Field("userName") username: String): Call<ApiResponse<UsernameAvailabilityResponse>>

    @POST("user/getData")
    @FormUrlEncoded
    fun getInterests(@Field("flag") flag: Int = ApiConstants.FLAG_INTERESTS): Call<ApiResponse<List<InterestDto>>>

    @POST("user/addEditVenueGroup")
    fun createVenue(@Body request: CreateEditVenueRequest): Call<ApiResponse<VenueDto>>

    @POST("user/getData")
    @FormUrlEncoded
    fun getVenues(@Field("flag") flag: Int = ApiConstants.FLAG_GET_VENUES,
                  @Field("currentLat") latitude: Double? = null,
                  @Field("currentLong") longitude: Double? = null): Call<ApiResponse<GetVenuesResponse>>

    @POST("user/getVenueFilter")
    @FormUrlEncoded
    fun getVenuesWithFilter(@Field("categoryId") categoryId: String? = null,
                            @Field("date") date: String? = null,
                            @Field("private") isPrivate: Int? = null,
                            @Field("locationLat") latitude: Double? = null,
                            @Field("locationLong") longitude: Double? = null): Call<ApiResponse<List<VenueDto>>>

    @POST("user/updateUserCategories")
    @FormUrlEncoded
    fun updateInterests(@Field("categoryArray") interests: List<String>): Call<ApiResponse<ProfileDto>>

    @POST("user/joinGroup")
    @FormUrlEncoded
    fun joinVenue(@Field("groupId") venueId: String,
                  @Field("adminId") adminId: String,
                  @Field("isPrivate") isPrivate: Boolean,
                  @Field("groupType") type: String = ApiConstants.TYPE_VENUE): Call<Any>

    @POST("user/venueConversationDetails")
    @FormUrlEncoded
    fun getVenueDetails(@Field("groupId") venueId: String?,
                        @Field("chatId") lastMessageId: String?): Call<ApiResponse<VenueDetailsResponse>>

    @POST("user/configNotification")
    @FormUrlEncoded
    fun changeVenueNotifications(@Field("venueId") venueId: String,
                                 @Field("action") isEnabled: Boolean): Call<Any>

    @POST("user/exitGroup")
    @FormUrlEncoded
    fun exitVenue(@Field("venueId") venueId: String): Call<Any>

    @POST("user/getData")
    @FormUrlEncoded
    fun getGroups(@Field("flag") flag: Int = ApiConstants.FLAG_GET_GROUPS): Call<ApiResponse<GetGroupsResponse>>

    @POST("user/addEditPostGroup")
    fun createGroup(@Body request: CreateEditGroupRequest): Call<Any>

    @POST("user/getCatPostGroups")
    @FormUrlEncoded
    fun getTopicGroups(@Field("categoryId") topicId: String,
                       @Field("pageNo") page: Int,
                       @Field("limit") limit: Int): Call<ApiResponse<List<GroupDto>>>

    @POST("user/joinGroup")
    @FormUrlEncoded
    fun joinGroup(@Field("groupId") groupId: String,
                  @Field("adminId") adminId: String,
                  @Field("isPrivate") isPrivate: Boolean,
                  @Field("groupType") type: String = ApiConstants.TYPE_GROUP): Call<Any>

    @POST("user/postGroupConversation")
    @FormUrlEncoded
    fun getGroupPosts(@Field("groupId") groupId: String,
                      @Field("pageNo") page: Int,
                      @Field("limit") limit: Int): Call<ApiResponse<GetGroupPostsResponse>>

    @POST("user/logOut")
    fun logout(): Call<Any>
}