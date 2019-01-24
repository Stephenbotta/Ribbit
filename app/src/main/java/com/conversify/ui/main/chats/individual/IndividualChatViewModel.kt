package com.conversify.ui.main.chats.individual

import android.app.Application
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.chat.ChatListingDto
import com.conversify.ui.base.BaseViewModel
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Manish Bhargav
 */
class IndividualChatViewModel(application: Application) : BaseViewModel(application) {

    val chatSummary by lazy { SingleLiveEvent<Resource<List<Any>>>() }
    private val chatList by lazy { mutableListOf<ChatListingDto>() }
    private var searchQuery = ""

    fun getChatSummary() {
        chatSummary.value = Resource.loading()

        RetrofitClient.conversifyApi
                .getChatSummary(1.0)
                .enqueue(object : Callback<ApiResponse<List<ChatListingDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<ChatListingDto>>>,
                                            response: Response<ApiResponse<List<ChatListingDto>>>) {
                        if (response.isSuccessful) {
                            val data = response.body()?.data ?: emptyList()

                            this@IndividualChatViewModel.chatList.clear()
                            this@IndividualChatViewModel.chatList.addAll(data)

                            val groupItems = if (searchQuery.isBlank()) {
                                getGroupsItems(data)
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
    private fun getSearchGroupsResult(query: String): List<Any> {
        // If query is blank then return the result with all groups
        if (query.isBlank()) {
            return getGroupsItems(chatList)
        }

        val yourGroupsResult = chatList.filter {
            (it.profile?.userName ?: "").toLowerCase().contains(query.toLowerCase())
        }

        return getGroupsItems(yourGroupsResult)
    }

    /**
     * Returns chat items in correct order
     * */
    private fun getGroupsItems(chatList: List<ChatListingDto>): List<Any> {
        val groupItems = mutableListOf<Any>()

        if (chatList.isNotEmpty()) {
            groupItems.addAll(chatList)
        }

        return groupItems
    }


}