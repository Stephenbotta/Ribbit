package com.conversify.ui.post.details

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.PagingResult
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.groups.GroupPostDto
import com.conversify.data.remote.models.post.AddPostReplyRequest
import com.conversify.data.remote.models.post.PostDetailsHeader
import com.conversify.data.remote.models.post.PostReplyDto
import com.conversify.data.remote.models.post.SubReplyDto
import com.conversify.utils.AppUtils
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostDetailsViewModel : ViewModel() {
    val post by lazy { MutableLiveData<Resource<GroupPostDto>>() }
    val replies by lazy { SingleLiveEvent<Resource<PagingResult<List<PostReplyDto>>>>() }
    val subReplies by lazy { SingleLiveEvent<Resource<SubReplyDto>>() }
    val addPostReply by lazy { SingleLiveEvent<Resource<PostReplyDto>>() }
    val likeUnlikePost by lazy { SingleLiveEvent<Resource<Any>>() }

    private lateinit var postDetailsHeader: PostDetailsHeader

    private var likeUnlikePostCall: Call<Any>? = null

    fun start(groupPost: GroupPostDto) {
        postDetailsHeader = PostDetailsHeader(groupPost)
    }

    fun getPostDetailsHeader() = postDetailsHeader

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
        val lastParentSubReplyId = parentReply.subReplies.firstOrNull()?.id
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
                            }

                            // Update sub-replies for the parent post
                            val existingSubReplies = parentReply.subReplies
                            val updatedSubReplies = mutableListOf<PostReplyDto>()
                            updatedSubReplies.addAll(existingSubReplies)
                            updatedSubReplies.addAll(0, receivedSubReplies)
                            parentReply.subReplies = updatedSubReplies

                            // Update pending and visible reply count
                            val totalReplyCount = parentReply.replyCount ?: 0
                            parentReply.pendingReplyCount = totalReplyCount - updatedSubReplies.size
                            parentReply.visibleReplyCount = updatedSubReplies.size

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
        val usernameMentions = AppUtils.getMentionsFromString(replyText)
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
                            postDetailsHeader.groupPost.repliesCount = (postDetailsHeader.groupPost.repliesCount
                                    ?: 0) + 1
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

    fun likeUnlikePost(isLiked: Boolean) {
        likeUnlikePost.value = Resource.loading()
        postDetailsHeader.groupPost.isLiked = isLiked
        val currentLikesCount = postDetailsHeader.groupPost.likesCount ?: 0
        postDetailsHeader.groupPost.likesCount = if (isLiked) {
            currentLikesCount + 1
        } else {
            currentLikesCount - 1
        }

        val postId = postDetailsHeader.groupPost.id ?: ""
        val postOwnerId = postDetailsHeader.groupPost.user?.id ?: ""
        val action = if (isLiked) ApiConstants.LIKED_TRUE else ApiConstants.LIKED_FALSE
        likeUnlikePostCall?.cancel()
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
}