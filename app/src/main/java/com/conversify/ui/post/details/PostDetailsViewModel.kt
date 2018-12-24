package com.conversify.ui.post.details

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.PagingResult
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.groups.GroupPostDto
import com.conversify.data.remote.models.post.PostReplyDto
import com.conversify.data.remote.models.post.SubReplyDto
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostDetailsViewModel : ViewModel() {
    val post by lazy { MutableLiveData<Resource<GroupPostDto>>() }
    val replies by lazy { SingleLiveEvent<Resource<PagingResult<List<PostReplyDto>>>>() }
    val subReplies by lazy { SingleLiveEvent<Resource<SubReplyDto>>() }

    private lateinit var groupPost: GroupPostDto

    fun start(groupPost: GroupPostDto) {
        this.groupPost = groupPost
    }

    fun getPostWithReplies() {
        replies.value = Resource.loading()
        RetrofitClient.conversifyApi
                .getPostWithReplies(groupPost.id ?: "")
                .enqueue(object : Callback<ApiResponse<GroupPostDto>> {
                    override fun onResponse(call: Call<ApiResponse<GroupPostDto>>,
                                            response: Response<ApiResponse<GroupPostDto>>) {
                        if (response.isSuccessful) {
                            val receivedReplies = response.body()?.data?.replies ?: emptyList()
                            receivedReplies.forEach { reply ->
                                reply.pendingReplyCount = reply.replyCount ?: 0
                            }
                            replies.value = Resource.success(PagingResult(true, receivedReplies))
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
}