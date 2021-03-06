package com.ribbit.ui.main.chats.individual

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
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
import kotlinx.android.synthetic.main.fragment_individual_chat.*

class IndividualChatFragment : BaseFragment(), ChatListCallback {
    companion object {
        const val TAG = "IndividualChatFragment"
    }

    private val viewModel by lazy { ViewModelProviders.of(this)[IndividualChatViewModel::class.java] }
    private lateinit var adapter: ChatListCommonAdapter

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

        searchChatView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String?): Boolean {
                search(query ?: "")
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        })
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
                        rvIndividualChat.visible()
                    } else {
                        tvLabelEmptyChat.visible()
                    }
                    adapter.displayCategories(chats)
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

    override fun onClickItem(chat: ChatListingDto) {
        val userCrossed = UserCrossedDto()
        userCrossed.conversationId = chat.conversationId
        userCrossed.profile = chat.profile
        val intent = ChatActivity.getStartIntentForIndividualChat(requireContext(), userCrossed, AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT)
        startActivityForResult(intent, AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT)
    }
}