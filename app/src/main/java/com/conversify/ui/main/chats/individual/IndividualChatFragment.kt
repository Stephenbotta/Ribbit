package com.conversify.ui.main.chats.individual

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.chat.ChatListingDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.main.chats.ChatListCallback
import com.conversify.ui.main.chats.ChatListCommonAdapter
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_individual_chat.*

class IndividualChatFragment : BaseFragment(), ChatListCallback {
    companion object {
        const val TAG = "IndividualChatFragment"
    }

    private lateinit var adapter: ChatListCommonAdapter
    private lateinit var viewModel: IndividualChatViewModel
    private lateinit var items: List<Any>

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_individual_chat


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = ChatListCommonAdapter(GlideApp.with(this), this)
        viewModel = ViewModelProviders.of(this).get(IndividualChatViewModel::class.java)
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
                    tvLabelEmptyChat.visibility = View.GONE
                    rvIndividualChat.visibility = View.VISIBLE
                    adapter.displayCategories(items)
                }

                Status.ERROR -> {
                    swipeRefreshLayout.isRefreshing = false
                    tvLabelEmptyChat.visibility = View.VISIBLE
                    rvIndividualChat.visibility = View.GONE
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
        if (item is ChatListingDto)
            Toast.makeText(context, item.profile?.userName, Toast.LENGTH_LONG).show()
    }
}