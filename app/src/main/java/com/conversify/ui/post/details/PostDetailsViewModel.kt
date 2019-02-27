package com.conversify.ui.post.details

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.support.v4.util.ArrayMap
import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.PagingResult
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.groups.GroupPostDto
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.post.*
import com.conversify.utils.AppUtils
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class PostDetailsViewModel : ViewModel() {
    val post by lazy { MutableLiveData<Resource<GroupPostDto>>() }
    val replies by lazy { SingleLiveEvent<Resource<PagingResult<List<PostReplyDto>>>>() }
    val subReplies by lazy { SingleLiveEvent<Resource<SubReplyDto>>() }
    val addPostReply by lazy { SingleLiveEvent<Resource<PostReplyDto>>() }
    val addPostSubReply by lazy { SingleLiveEvent<Resource<PostReplyDto>>() }
    val likeUnlikePost by lazy { SingleLiveEvent<Resource<Any>>() }
    val likeUnlikeReply by lazy { SingleLiveEvent<Resource<Any>>() }
    val mentionSuggestions by lazy { SingleLiveEvent<Resource<List<ProfileDto>>>() }
    val deletePost by lazy { SingleLiveEvent<Resource<Any>>() }

    /*
    * Contains replyId as key and its respective api call as value.
    *
    * Contains on-going api calls for like-unlike a reply (both top level and sub-replies).
    * This is to avoid multiple on-going api calls for same reply which can cause de-synced state of like button.
    * Previous call is first canceled before calling a new one for the same reply id.
    * */
    private val likeUnlikeReplyCalls by lazy { ArrayMap<String, Call<Any>>() }

    private lateinit var postDetailsHeader: PostDetailsHeader

    private var likeUnlikePostCall: Call<Any>? = null
    private var mentionSuggestionsCall: Call<*>? = null

    fun start(groupPost: GroupPostDto) {
        postDetailsHeader = PostDetailsHeader(groupPost)
    }

    fun getPostDetailsHeader() = postDetailsHeader

    fun updateHeaderPost(post: GroupPostDto) {
        postDetailsHeader.groupPost = post
    }

    fun getGroupPost() = postDetailsHeader.groupPost

    fun isPostLiked() = postDetailsHeader.groupPost.isLiked ?: false

    fun getPostWithReplies(firstPage: Boolean = true) {
        replies.value = Resource.loading()
        RetrofitClient.conversifyApi
                .getPostWithReplies(postDetailsHeader.groupPost.id ?: "")
                .enqueue(object : Callback<ApiResponse<GroupPostDto>> {
                    override fun onResponse(call: Call<ApiResponse<GroupPostDto>>,
                                            response: Response<ApiResponse<GroupPostDto>>) {
                        if (response.isSuccessful) {
                            val receivedGroupPost = response.body()?.data
                            if (receivedGroupPost != null) {
                                postDetailsHeader.groupPost = receivedGroupPost   // Update group post
                                val receivedReplies = receivedGroupPost.replies ?: emptyList()
                                receivedReplies.forEach { reply ->
                                    reply.pendingReplyCount = reply.replyCount ?: 0
                                }
                                replies.value = Resource.success(PagingResult(firstPage, receivedReplies))
                            } else {
                                replies.value = Resource.success(PagingResult(firstPage, emptyList()))
                            }
                        } else {
                            replies.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<GroupPostDto>>, t: Throwable) {
                        replies.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun getSubReplies(parentReply: PostReplyDto) {
        parentReply.subRepliesLoading = true
        subReplies.value = Resource(Status.LOADING,
                SubReplyDto(parentReply, emptyList()),
                null)
        val parentReplyId = parentReply.id ?: ""
        val parentTotalSubRepliesCount = parentReply.replyCount ?: 0
        val lastParentSubReplyId = if (parentReply.visibleReplyCount > 0) {
            parentReply.subReplies.firstOrNull()?.id
        } else {
            null
        }
        RetrofitClient.conversifyApi
                .getSubReplies(parentReplyId, parentTotalSubRepliesCount, lastParentSubReplyId)
                .enqueue(object : Callback<ApiResponse<List<PostReplyDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<PostReplyDto>>>,
                                            response: Response<ApiResponse<List<PostReplyDto>>>) {
                        // Set sub-replies loading to false
                        parentReply.subRepliesLoading = false

                        if (response.isSuccessful) {
                            val receivedSubReplies = response.body()?.data ?: emptyList()
                            receivedSubReplies.forEach { receivedSubReply ->
                                receivedSubReply.parentReplyId = parentReplyId
                                receivedSubReply.parentReplyOwnerId = parentReply.commentBy?.id
                            }

                            // Add received sub-replies to the parent post at the top
                            parentReply.subReplies.addAll(0, receivedSubReplies)

                            // Update pending and visible reply count
                            val totalReplyCount = parentReply.replyCount ?: 0
                            val subRepliesCount = parentReply.subReplies.size
                            parentReply.visibleReplyCount = subRepliesCount
                            parentReply.pendingReplyCount = totalReplyCount - subRepliesCount

                            val subReply = SubReplyDto(parentReply, receivedSubReplies)
                            subReplies.value = Resource.success(subReply)
                        } else {
                            subReplies.value = Resource(Status.ERROR,
                                    SubReplyDto(parentReply, emptyList()),
                                    response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<List<PostReplyDto>>>, t: Throwable) {
                        parentReply.subRepliesLoading = false
                        subReplies.value = Resource(Status.ERROR,
                                SubReplyDto(parentReply, emptyList()),
                                t.failureAppError())
                    }
                })
    }

    fun addPostReply(replyText: String) {
        val usernameMentions = AppUtils.getMentionsFromString(replyText, false)
        val request = AddPostReplyRequest(postId = postDetailsHeader.groupPost.id,
                postOwnerId = postDetailsHeader.groupPost.user?.id,
                replyText = replyText,
                usernameMentions = if (usernameMentions.isEmpty()) null else usernameMentions)

        addPostReply.value = Resource.loading()
        RetrofitClient.conversifyApi
                .addPostReply(request)
                .enqueue(object : Callback<ApiResponse<PostReplyDto>> {
                    override fun onResponse(call: Call<ApiResponse<PostReplyDto>>,
                                            response: Response<ApiResponse<PostReplyDto>>) {
                        if (response.isSuccessful) {
                            incrementHeaderRepliesCount()
                            addPostReply.value = Resource.success(response.body()?.data)
                        } else {
                            addPostReply.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<PostReplyDto>>, t: Throwable) {
                        addPostReply.value = Resource.error(t.failureAppError())
                    }
                })
    }

    private fun incrementHeaderRepliesCount() {
        val post = postDetailsHeader.groupPost
        post.repliesCount = (post.repliesCount ?: 0) + 1
    }

    fun addPostSubReply(replyText: String, topLevelReply: PostReplyDto) {
        val usernameMentions = AppUtils.getMentionsFromString(replyText, false)
        val request = AddPostSubReplyRequest(postId = postDetailsHeader.groupPost.id,
                topLevelReplyId = topLevelReply.id,
                topLevelReplyOwnerId = topLevelReply.commentBy?.id,
                replyText = replyText,
                usernameMentions = if (usernameMentions.isEmpty()) null else usernameMentions)

        addPostSubReply.value = Resource.loading()
        RetrofitClient.conversifyApi
                .addPostSubReply(request)
                .enqueue(object : Callback<ApiResponse<PostReplyDto>> {
                    override fun onResponse(call: Call<ApiResponse<PostReplyDto>>,
                                            response: Response<ApiResponse<PostReplyDto>>) {
                        if (response.isSuccessful) {
                            val addedSubReply = response.body()?.data
                            if (addedSubReply != null) {
                                // Update parent reply id
                                addedSubReply.parentReplyId = topLevelReply.id
                                addedSubReply.parentReplyOwnerId = topLevelReply.commentBy?.id

                                // Add to existing sub-replies list
                                val topLevelTotalReplyCount = topLevelReply.replyCount ?: 0

                                if (topLevelTotalReplyCount == 0 || topLevelTotalReplyCount != topLevelReply.pendingReplyCount) {
                                    Timber.i("Adding reply")
                                    topLevelReply.subReplies.add(addedSubReply)
                                }

                                // Update total and pending reply count
                                val newTotalReplyCount = topLevelTotalReplyCount + 1
                                topLevelReply.replyCount = newTotalReplyCount

                                // Increment visible reply count if it is non-zero
                                if (topLevelReply.visibleReplyCount > 0) {
                                    topLevelReply.visibleReplyCount += 1
                                }

                                // Update pending reply count
                                topLevelReply.pendingReplyCount = newTotalReplyCount - topLevelReply.subReplies.size

                                incrementHeaderRepliesCount()
                            }
                            addPostSubReply.value = Resource.success(addedSubReply)
                        } else {
                            addPostSubReply.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<PostReplyDto>>, t: Throwable) {
                        addPostSubReply.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun likeUnlikePost(post: GroupPostDto, isLiked: Boolean) {
        likeUnlikePost.value = Resource.loading()

        val postId = post.id ?: ""
        val postOwnerId = post.user?.id ?: ""
        val action = if (isLiked) ApiConstants.LIKED_TRUE else ApiConstants.LIKED_FALSE
        likeUnlikePostCall?.cancel()    // Cancel any on-going api call for like unlike post
        val call = RetrofitClient.conversifyApi.likeUnlikePost(postId, postOwnerId, action)
        likeUnlikePostCall = call
        call.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    likeUnlikePost.value = Resource.success()
                } else {
                    likeUnlikePost.value = Resource.error(response.getAppError())
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                if (!call.isCanceled) {
                    likeUnlikePost.value = Resource.error(t.failureAppError())
                }
            }
        })
    }

    /**
     * Should be used only when group post is passed in the start()
     * */
    fun likeUnlikePost(isLiked: Boolean) {
        // Update liked status and likes count for header
        postDetailsHeader.groupPost.isLiked = isLiked
        val currentLikesCount = postDetailsHeader.groupPost.likesCount ?: 0
        postDetailsHeader.groupPost.likesCount = if (isLiked) {
            currentLikesCount + 1
        } else {
            currentLikesCount - 1
        }

        likeUnlikePost(postDetailsHeader.groupPost, isLiked)
    }

    fun likeUnlikeReply(reply: PostReplyDto, isLiked: Boolean, topLevelReply: Boolean) {
        likeUnlikeReply.value = Resource.loading()

        val replyId = reply.id ?: ""
        val replyOwnerId = if (topLevelReply) {
            reply.commentBy?.id
        } else {
            reply.replyBy?.id
        } ?: ""
        val action = if (isLiked) ApiConstants.LIKED_TRUE else ApiConstants.LIKED_FALSE

        // Use the api according to the reply level
        val call = if (topLevelReply) {
            RetrofitClient.conversifyApi.likeUnlikeReply(replyId, replyOwnerId, action)
        } else {
            RetrofitClient.conversifyApi.likeUnlikeSubReply(replyId, replyOwnerId, action)
        }

        // If any call already exist for the replyId, then first cancel it.
        likeUnlikeReplyCalls[replyId]?.cancel()

        // Add the new call for the replyId
        likeUnlikeReplyCalls[replyId] = call

        call.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // Remove the call
                likeUnlikeReplyCalls.remove(replyId)

                if (response.isSuccessful) {
                    likeUnlikeReply.value = Resource.success()
                } else {
                    likeUnlikeReply.value = Resource.error(response.getAppError())
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                // Remove the call
                likeUnlikeReplyCalls.remove(replyId)

                if (!call.isCanceled) {
                    likeUnlikeReply.value = Resource.error(t.failureAppError())
                }
            }
        })
    }

    fun getMentionSuggestions(mentionText: String) {
        mentionSuggestions.value = Resource.loading()

        val call = RetrofitClient.conversifyApi.getMentionSuggestions(mentionText)
        mentionSuggestionsCall?.cancel()    // Cancel any on-going call first
        mentionSuggestionsCall = call   // Update call with the latest one

        call.enqueue(object : Callback<ApiResponse<List<ProfileDto>>> {
            override fun onResponse(call: Call<ApiResponse<List<ProfileDto>>>,
                                    response: Response<ApiResponse<List<ProfileDto>>>) {
                if (response.isSuccessful) {
                    mentionSuggestions.value = Resource.success(response.body()?.data)
                } else {
                    mentionSuggestions.value = Resource.error(response.getAppError())
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<ProfileDto>>>, t: Throwable) {
                if (!call.isCanceled) {
                    mentionSuggestions.value = Resource.error(t.failureAppError())
                }
            }
        })
    }

    fun deletePost(postId: String) {
        deletePost.value = Resource.loading()

        RetrofitClient.conversifyApi
                .deleteGroupPost(postId)
                .enqueue(object : Callback<ApiResponse<Any>> {
                    override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                        if (response.isSuccessful) {
                            deletePost.value = Resource.success(response.body()?.data)
                        } else {
                            deletePost.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                        deletePost.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun cancelGetMentionSuggestions() {
        mentionSuggestionsCall?.cancel()
    }
}