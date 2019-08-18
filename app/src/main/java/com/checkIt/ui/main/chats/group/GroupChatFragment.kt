package com.checkIt.ui.main.chats.group

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.checkIt.R
import com.checkIt.data.local.PrefsManager
import com.checkIt.data.remote.models.Status
import com.checkIt.data.remote.models.chat.ChatListingDto
import com.checkIt.data.remote.models.people.UserCrossedDto
import com.checkIt.extensions.gone
import com.checkIt.extensions.handleError
import com.checkIt.extensions.isNetworkActiveWithMessage
import com.checkIt.extensions.visible
import com.checkIt.ui.base.BaseFragment
import com.checkIt.ui.chat.ChatActivity
import com.checkIt.ui.main.chats.ChatListCallback
import com.checkIt.ui.main.chats.ChatListCommonAdapter
import com.checkIt.utils.AppConstants
import com.checkIt.utils.GlideApp
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
                    if (items.isNotEmpty()) {
                        tvLabelEmptyChat.gone()
                        rvGroupChat.visible()
                    } else {
                        tvLabelEmptyChat.visible()
                    }
                    adapter.displayCategories(items)
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

    override fun onClickItem() {
    }

    override fun onClickItem(position: Int) {
        val item = items[position]
        if (item is ChatListingDto) {
            val userCrossed = UserCrossedDto()
            userCrossed.conversationId = item.conversationId
            userCrossed.profile = item.profile
            PrefsManager.get().save(PrefsManager.PREF_CHAT_TYPE, true)
            val intent = ChatActivity.getStartIntentForIndividualChat(requireContext(), userCrossed, AppConstants.REQ_CODE_LISTING_GROUP_CHAT)
            startActivityForResult(intent, AppConstants.REQ_CODE_LISTING_GROUP_CHAT)
        }
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