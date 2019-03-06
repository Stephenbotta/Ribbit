package com.conversify.data.remote

import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.chat.ChatListingDto
import com.conversify.data.remote.models.chat.VenueDetailsResponse
import com.conversify.data.remote.models.groups.*
import com.conversify.data.remote.models.loginsignup.*
import com.conversify.data.remote.models.notifications.NotificationDto
import com.conversify.data.remote.models.people.GetPeopleResponse
import com.conversify.data.remote.models.post.AddPostReplyRequest
import com.conversify.data.remote.models.post.AddPostSubReplyRequest
import com.conversify.data.remote.models.post.CreatePostRequest
import com.conversify.data.remote.models.post.PostReplyDto
import com.conversify.data.remote.models.profile.CreateEditProfileRequest
import com.conversify.data.remote.models.venues.*
import com.conversify.ui.chat.individual.ChatIndividualResponse
import retrofit2.Call
import retrofit2.http.*

interface ConversifyApi {

    companion object {
        private const val FLAG = "flag"
        private const val ACTION = "action"
    }

    @POST("user/regEmailOrPhone")
    @FormUrlEncoded
    fun registerEmailOrPhoneNumber(@Field("email") email: String? = null,
                                   @Field("countryCode") countryCode: String? = null,
                                   @Field("phoneNumber") phoneNumber: String? = null): Call<ApiResponse<ProfileDto>>

    @POST("user/logIn")
    fun login(@Body request: LoginRequest): Call<ApiResponse<ProfileDto>>

    @POST("user/forgotPassword")
    @FormUrlEncoded
    fun resetPassword(@Field("email") email: String): Call<ApiResponse<Any>>

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
    fun getInterests(@Field(FLAG) flag: Int = ApiConstants.FLAG_INTERESTS): Call<ApiResponse<List<InterestDto>>>

    @POST("user/addEditVenueGroup")
    fun createVenue(@Body request: CreateEditVenueRequest): Call<ApiResponse<VenueDto>>

    @POST("user/getData")
    @FormUrlEncoded
    fun getVenues(@Field(FLAG) flag: Int = ApiConstants.FLAG_GET_VENUES,
                  @Field("currentLat") latitude: Double? = null,
                  @Field("currentLong") longitude: Double? = null): Call<ApiResponse<GetVenuesResponse>>

    @POST("user/getVenueFilter")
    fun getVenuesWithFilter(@Body request: GetVenuesWithFilterRequest): Call<ApiResponse<List<VenueDto>>>

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
    fun getVenueDetailsForChat(@Field("groupId") venueId: String?,
                               @Field("chatId") lastMessageId: String?): Call<ApiResponse<VenueDetailsResponse>>

    @POST("user/configNotification")
    @FormUrlEncoded
    fun changeVenueNotifications(@Field("venueId") venueId: String,
                                 @Field(ACTION) isEnabled: Boolean): Call<Any>

    @POST("user/exitGroup")
    @FormUrlEncoded
    fun exitVenue(@Field("venueId") venueId: String): Call<Any>

    @POST("user/exitGroup")
    @FormUrlEncoded
    fun exitGroup(@Field("groupId") groupId: String): Call<Any>

    @POST("user/archiveGroup")
    @FormUrlEncoded
    fun archiveVenue(@Field("groupId") venueId: String,
                     @Field("groupType") type: String): Call<Any>

    @POST("user/getData")
    @FormUrlEncoded
    fun getGroups(@Field(FLAG) flag: Int = ApiConstants.FLAG_GET_GROUPS): Call<ApiResponse<GetGroupsResponse>>

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

    @POST("user/getNotifications")
    @FormUrlEncoded
    fun getNotifications(@Field("pageNo") page: Int): Call<ApiResponse<List<NotificationDto>>>

    @POST("user/clearNotification")
    @FormUrlEncoded
    fun clearNotification(@Field("pageNo") page: Int = 1): Call<ApiResponse<List<NotificationDto>>>

    @POST("user/acceptInviteRequest")
    @FormUrlEncoded
    fun acceptRejectInviteRequest(@Field("acceptType") acceptType: String,
                                  @Field("groupType") groupType: String,
                                  @Field("userId") userId: String,
                                  @Field("groupId") groupId: String,
                                  @Field("accept") accept: Boolean): Call<Any>

    @POST("user/acceptFollowRequest")
    @FormUrlEncoded
    fun acceptFollowRequest(@Field("userId") userId: String,
                            @Field("action") action: Boolean): Call<Any>

    @POST("user/logOut")
    fun logout(): Call<Any>

    @POST("user/getData")
    @FormUrlEncoded
    fun getHomeFeed(@Field(FLAG) flag: Int = ApiConstants.FLAG_GET_HOME_FEED,
                    @Field("pageNo") page: Int,
                    @Field("limit") limit: Int): Call<ApiResponse<List<GroupPostDto>>>

    @POST("user/getData")
    @FormUrlEncoded
    fun getYourGroups(@Field(FLAG) flag: Int = ApiConstants.FLAG_GET_YOUR_GROUPS): Call<ApiResponse<List<GroupDto>>>

    @POST("user/addEditPost")
    fun createPost(@Body request: CreatePostRequest): Call<Any>

    @POST("user/getPostWithComment")
    @FormUrlEncoded
    fun getPostWithReplies(@Field("postId") postId: String): Call<ApiResponse<GroupPostDto>>

    @POST("user/getCommentReplies")
    @FormUrlEncoded
    fun getSubReplies(@Field("commentId") parentReplyId: String,
                      @Field("totalReply") parentTotalSubRepliesCount: Int,
                      @Field("replyId") lastParentSubReplyId: String? = null): Call<ApiResponse<List<PostReplyDto>>>

    @POST("user/addEditComment")
    fun addPostReply(@Body request: AddPostReplyRequest): Call<ApiResponse<PostReplyDto>>

    @POST("user/likeOrUnlike")
    @FormUrlEncoded
    fun likeUnlikePost(@Field("postId") postId: String,
                       @Field("postBy") postOwnerId: String,
                       @Field(ACTION) action: Int): Call<Any>

    @POST("user/likeOrUnlike")
    @FormUrlEncoded
    fun likeUnlikeReply(@Field("commentId") replyId: String,
                        @Field("commentBy") replyOwnerId: String,
                        @Field(ACTION) action: Int): Call<Any>

    @POST("user/likeOrUnlike")
    @FormUrlEncoded
    fun likeUnlikeSubReply(@Field("replyId") subReplyId: String,
                           @Field("replyBy") subReplyOwnerId: String,
                           @Field(ACTION) action: Int): Call<Any>

    @POST("user/addEditReplies")
    fun addPostSubReply(@Body request: AddPostSubReplyRequest): Call<ApiResponse<PostReplyDto>>

    @POST("user/searchUser")
    @FormUrlEncoded
    fun getMentionSuggestions(@Field("search") username: String): Call<ApiResponse<List<ProfileDto>>>

    @POST("user/groupDetails")
    @FormUrlEncoded
    fun getVenueDetails(@Field("venueId") venueId: String): Call<ApiResponse<VenueDto>>

    @POST("user/addParticipantsList")
    @FormUrlEncoded
    fun getVenueAddParticipants(@FieldMap map: HashMap<String, String>?): Call<ApiResponse<List<ProfileDto>>>

    @POST("user/addParticipants")
    fun addVenueParticipants(@Body request: AddVenueParticipantsRequest): Call<Any>

    @POST("user/crossedPeople")
    fun getCrossedPeople(): Call<ApiResponse<List<GetPeopleResponse>>>

    @POST("user/getProfileData")
    @FormUrlEncoded
    fun getUserProfileDetails(@FieldMap map: HashMap<String, String>?): Call<ApiResponse<ProfileDto>>

    @POST("user/followUnfollow")
    @FormUrlEncoded
    fun postFollowUnFollow(@Field("userId") userId: String,
                           @Field(ACTION) action: Double): Call<ApiResponse<Any>>

    @POST("user/blockUser")
    @FormUrlEncoded
    fun postBlock(@Field("userId") userId: String,
                  @Field(ACTION) action: Double): Call<ApiResponse<Any>>

    @POST("user/followUnfollowTag")
    @FormUrlEncoded
    fun postFollowUnFollowTag(@Field("tagId") tagId: String,
                              @Field("follow") follow: Boolean): Call<ApiResponse<Any>>

    @POST("user/chatConversation")
    @FormUrlEncoded
    fun getIndividualChat(@Field("conversationId") conversationId: String?,
                          @Field("chatId") chatId: String?): Call<ApiResponse<ChatIndividualResponse>>

    @POST("user/chatSummary")
    @FormUrlEncoded
    fun getChatSummary(@Field(FLAG) flag: Double?): Call<ApiResponse<List<ChatListingDto>>>

    @POST("user/groupDetails")
    @FormUrlEncoded
    fun getGroupDetails(@Field("groupId") groupId: String): Call<ApiResponse<GroupDto>>

    @POST("user/homeSearchTop")
    @FormUrlEncoded
    fun getTopSearch(@FieldMap map: HashMap<String, String>?): Call<ApiResponse<List<ProfileDto>>>

    @POST("user/homeSearchTag")
    @FormUrlEncoded
    fun getTagSearch(@FieldMap map: HashMap<String, String>?): Call<ApiResponse<List<ProfileDto>>>

    @POST("user/homeSearchGroup")
    @FormUrlEncoded
    fun getGroupSearch(@FieldMap map: HashMap<String, String>?): Call<ApiResponse<List<GroupDto>>>

    @POST("user/homeSearchVenue")
    @FormUrlEncoded
    fun getVenueSearch(@FieldMap map: HashMap<String, String>?): Call<ApiResponse<List<VenueDto>>>

    @POST("user/homeSearchPost")
    @FormUrlEncoded
    fun getPostSearch(@FieldMap map: HashMap<String, String>?): Call<ApiResponse<List<GroupPostDto>>>

    @POST("user/editProfile")
    fun editProfile(@Body request: CreateEditProfileRequest): Call<ApiResponse<ProfileDto>>

    @POST("user/settingVerification")
    @FormUrlEncoded
    fun postSettingsVerification(@FieldMap map: HashMap<String, String>?): Call<ApiResponse<Any>>

    @GET("user/listBlockedUsers")
    fun getBlockedUsersList(): Call<ApiResponse<List<ProfileDto>>>

    @POST("user/configSetting")
    @FormUrlEncoded
    fun getAlertNotification(@Field(ACTION) action: Boolean, @Field(FLAG) flag: Int): Call<ApiResponse<ProfileDto>>

    @POST("user/configSetting")
    @FormUrlEncoded
    fun postConfigSetting(@FieldMap map: HashMap<String, String>?): Call<ApiResponse<ProfileDto>>

    @POST("user/configSetting")
    @FormUrlEncoded
    fun postConfigSettingUserArray(@Field(FLAG) flag: Int, @Field("userIds") userIds: List<String>): Call<ApiResponse<ProfileDto>>

    @POST("user/listFollowerFollowing")
    @FormUrlEncoded
    fun getFollowerFollowingList(@Field(FLAG) flag: Int): Call<ApiResponse<List<ProfileDto>>>

    @POST("user/deletePost")
    @FormUrlEncoded
    fun deleteGroupPost(@Field("postId") postId: String): Call<ApiResponse<Any>>

    @POST("user/updateDeviceToken")
    @FormUrlEncoded
    fun updateDeviceToken(@Field("deviceToken") deviceToken: String): Call<ApiResponse<Any>>

    @POST("user/interestMatchUsers")
    @FormUrlEncoded
    fun interestMatchUsers(@Field("locationLong") locationLong: Double,
                           @Field("locationLat") locationLat: Double,
                           @Field("range") range: Int,
                           @Field("pageNo") pageNo: Int,
                           @Field("categoryIds") categoryIds: List<String>): Call<ApiResponse<List<ProfileDto>>>

    @GET("user/requestCounts")
    fun getRequestCounts(): Call<ApiResponse<String>>
}