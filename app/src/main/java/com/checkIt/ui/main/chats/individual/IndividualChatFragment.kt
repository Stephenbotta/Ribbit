package com.checkIt.ui.main.chats.individual

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import com.checkIt.R
import com.checkIt.data.remote.models.Status
import com.checkIt.data.remote.models.chat.ChatListingDto
import com.checkIt.data.remote.models.people.UserCrossedDto
import com.checkIt.extensions.*
import com.checkIt.ui.base.BaseFragment
import com.checkIt.ui.chat.ChatActivity
import com.checkIt.ui.main.chats.ChatListCallback
import com.checkIt.ui.main.chats.ChatListCommonAdapter
import com.checkIt.utils.AppConstants
import com.checkIt.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_individual_chat.*

class IndividualChatFragment : BaseFragment(), ChatListCallback {
    companion object {
        const val TAG = "IndividualChatFragment"
    }

    private val viewModel by lazy { ViewModelProviders.of(this)[IndividualChatViewModel::class.java] }
    private lateinit var adapter: ChatListCommonAdapter
    private lateinit var items: List<Any>

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_individual_chat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = ChatListCommonAdapter(GlideApp.with(this), this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvIndividualChat.adapter = adapter
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
                        tvLabelEmptyChat.gone()
                        rvIndividualChat.visible()
                    } else {
                        tvLabelEmptyChat.visible()
                    }
                    adapter.displayCategories(items)
                }

                Status.ERROR -> {
                    swipeRefreshLayout.isRefreshing = false
                    tvLabelEmptyChat.visible()
                    rvIndividualChat.gone()
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
            val intent = ChatActivity.getStartIntentForIndividualChat(requireContext(), userCrossed, AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT)
            startActivityForResult(intent, AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT)
        }
    }
}