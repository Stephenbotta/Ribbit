package com.conversify.ui.main.chats.group

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.chat.ChatListingDto
import com.conversify.data.remote.models.people.UserCrossedDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.chat.ChatActivity
import com.conversify.ui.main.chats.ChatListCallback
import com.conversify.ui.main.chats.ChatListCommonAdapter
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_group_chat.*

class GroupChatFragment : BaseFragment(), ChatListCallback {

    companion object {
        const val TAG = "GroupChatFragment"
    }

    private val viewModel by lazy { ViewModelProviders.of(this)[GroupChatViewModel::class.java] }
    private lateinit var adapter: ChatListCommonAdapter
    private lateinit var items: List<Any>

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_group_chat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = ChatListCommonAdapter(GlideApp.with(this), this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvGroupChat.adapter = adapter
        swipeRefreshLayout.setOnRefreshListener { getChatSummary() }
        observeChanges()
        getChatSummary()
    }


    private fun observeChanges() {

        viewModel.chatSummary.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {

                Status.SUCCESS -> {
                    swipeRefreshLayout.isRefreshing = false
                    items = resource.data ?: emptyList()
                    if (items.size != 0) {
                        tvLabelEmptyChat.visibility = View.GONE
                        rvGroupChat.visibility = View.VISIBLE
                    }
                    adapter.displayCategories(items)
                }

                Status.ERROR -> {
                    swipeRefreshLayout.isRefreshing = false
                    tvLabelEmptyChat.visibility = View.VISIBLE
                    rvGroupChat.visibility = View.GONE
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    swipeRefreshLayout.isRefreshing = true
                }
            }

        })
    }

    fun search(query: String) {
        viewModel.searchGroups(query)
    }

    private fun getChatSummary() {
        if (isNetworkActiveWithMessage()) {
            viewModel.getChatSummary()
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onClickItem() {
    }

    override fun onClickItem(position: Int) {
        val item = items[position]
        if (item is ChatListingDto) {
            val userCrossed = UserCrossedDto()
            userCrossed.conversationId = item.conversationId
            userCrossed.profile = item.profile
            val intent = ChatActivity.getStartIntentForIndividualChat(requireContext(), userCrossed, AppConstants.REQ_CODE_LISTING_GROUP_CHAT)
            startActivityForResult(intent, AppConstants.REQ_CODE_LISTING_GROUP_CHAT)
        }
    }
}