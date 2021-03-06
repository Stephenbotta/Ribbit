package com.ribbit.ui.main.chats.group

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ribbit.R
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.chat.ChatListingDto
import com.ribbit.data.remote.models.people.UserCrossedDto
import com.ribbit.extensions.gone
import com.ribbit.extensions.handleError
import com.ribbit.extensions.isNetworkActiveWithMessage
import com.ribbit.extensions.visible
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.chat.ChatActivity
import com.ribbit.ui.main.chats.ChatListCallback
import com.ribbit.ui.main.chats.ChatListCommonAdapter
import com.ribbit.utils.AppConstants
import com.ribbit.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_group_chat.*

class GroupChatFragment : BaseFragment(), ChatListCallback {
    companion object {
        const val TAG = "GroupChatFragment"
    }

    private val viewModel by lazy { ViewModelProviders.of(this)[GroupChatViewModel::class.java] }
    private lateinit var adapter: ChatListCommonAdapter

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
                    val chats = resource.data ?: emptyList()
                    if (chats.isNotEmpty()) {
                        tvLabelEmptyChat.gone()
                        rvGroupChat.visible()
                    } else {
                        tvLabelEmptyChat.visible()
                    }
                    adapter.displayCategories(chats)
                }

                Status.ERROR -> {
                    swipeRefreshLayout.isRefreshing = false
                    tvLabelEmptyChat.visible()
                    rvGroupChat.gone()
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    swipeRefreshLayout.isRefreshing = true
                }
            }

        })
    }

    fun search(query: String?) {
        viewModel.searchGroups(query ?: "")
    }

    private fun getChatSummary() {
        if (isNetworkActiveWithMessage()) {
            viewModel.getChatSummary()
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onClickItem(chat: ChatListingDto) {
        val userCrossed = UserCrossedDto()
        userCrossed.conversationId = chat.conversationId
        userCrossed.profile = chat.profile
        val intent = ChatActivity.getStartIntentForIndividualChat(requireContext(), userCrossed,
                AppConstants.REQ_CODE_LISTING_GROUP_CHAT)
        startActivityForResult(intent, AppConstants.REQ_CODE_LISTING_GROUP_CHAT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstants.REQ_CODE_LISTING_GROUP_CHAT) {
            if (resultCode == Activity.RESULT_OK) {
                getChatSummary()
            }
        }
    }
}