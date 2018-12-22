package com.conversify.ui.post.details

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.PagingResult
import com.conversify.data.remote.models.Resource
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
}