package com.ribbit.ui.main.chats.group

import android.app.Application
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.chat.ChatListingDto
import com.ribbit.ui.base.BaseViewModel
import com.ribbit.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GroupChatViewModel(application: Application) : BaseViewModel(application) {

    val chatSummary by lazy { SingleLiveEvent<Resource<List<ChatListingDto>>>() }
    private val chatList by lazy { mutableListOf<ChatListingDto>() }
    private var searchQuery = ""

    fun getChatSummary() {
        chatSummary.value = Resource.loading()

        RetrofitClient.ribbitApi
                .getChatSummary(2.0)
                .enqueue(object : Callback<ApiResponse<List<ChatListingDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<ChatListingDto>>>,
                                            response: Response<ApiResponse<List<ChatListingDto>>>) {
                        if (response.isSuccessful) {
                            val data = response.body()?.data ?: emptyList()

                            this@GroupChatViewModel.chatList.clear()
                            this@GroupChatViewModel.chatList.addAll(data)

                            val groupItems = if (searchQuery.isBlank()) {
                                data
                            } else {
                                getSearchGroupsResult(searchQuery)
                            }

                            chatSummary.value = Resource.success(groupItems)
                        } else {
                            chatSummary.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<List<ChatListingDto>>>, t: Throwable) {
                        chatSummary.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun searchGroups(query: String) {
        this.searchQuery = query
        val searchResult = getSearchGroupsResult(query)
        chatSummary.value = Resource.success(searchResult)
    }

    /**
     * Returns chat list items in correct order after applying the search filter
     * */
    private fun getSearchGroupsResult(query: String): List<ChatListingDto> {
        // If query is blank then return the result with all groups
        if (query.isBlank()) {
            return chatList
        }

        return chatList.filter {
            (it.profile?.userName ?: "").toLowerCase().contains(query.toLowerCase())
        }
    }
}