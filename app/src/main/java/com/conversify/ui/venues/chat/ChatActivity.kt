package com.conversify.ui.venues.chat

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import com.conversify.R
import com.conversify.data.remote.models.PagingResult
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.chat.ChatMessageDto
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.venues.details.VenueDetailsActivity
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : BaseActivity(), ChatAdapter.Callback {
    companion object {
        private const val EXTRA_VENUE = "EXTRA_VENUE"

        fun start(context: Context, venue: VenueDto) {
            context.startActivity(Intent(context, ChatActivity::class.java)
                    .putExtra(EXTRA_VENUE, venue))
        }
    }

    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: ChatAdapter

    private val newMessageObserver = Observer<ChatMessageDto> {
        it ?: return@Observer
        setResult(Activity.RESULT_OK)
        adapter.addNewMessage(it)
        rvChat.scrollToPosition(adapter.itemCount - 1)
    }

    private val oldMessagesObserver = Observer<Resource<PagingResult<List<ChatMessageDto>>>> {
        it ?: return@Observer
        when (it.status) {
            Status.SUCCESS -> {
                swipeRefreshLayout.isRefreshing = false
                adapter.addOldMessages(it.data?.result ?: emptyList())
                // Scroll to bottom for first page
                if (it.data?.isFirstPage == true) {
                    rvChat.scrollToPosition(adapter.itemCount - 1)
                }
            }

            Status.ERROR -> {
                swipeRefreshLayout.isRefreshing = false
                handleError(it.error)
            }

            Status.LOADING -> {
                swipeRefreshLayout.isRefreshing = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val venue = intent.getParcelableExtra<VenueDto>(EXTRA_VENUE)
        viewModel = ViewModelProviders.of(this)[ChatViewModel::class.java]
        viewModel.start(venue)
        setListeners()
        observeChanges()
        setupChatRecycler()
        displayToolbarDetails(venue)
        getOldMessages()
    }

    private fun setListeners() {
        toolbar.setNavigationOnClickListener { onBackPressed() }

        ivVenue.setOnClickListener { showVenueDetails() }
        tvVenueName.setOnClickListener { showVenueDetails() }

        btnAttachment.setOnClickListener { }

        fabSend.setOnClickListener { sendTextMessage() }
    }

    private fun observeChanges() {
        viewModel.newMessage.observeForever(newMessageObserver)
        viewModel.oldMessages.observeForever(oldMessagesObserver)
    }

    private fun setupChatRecycler() {
        swipeRefreshLayout.isEnabled = false
        adapter = ChatAdapter(this, this)
        rvChat.adapter = adapter
        (rvChat.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        rvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!rvChat.canScrollVertically(-1) && viewModel.isValidForPaging()) {
                    viewModel.getOldMessages()
                }
            }
        })
    }

    private fun displayToolbarDetails(venue: VenueDto) {
        GlideApp.with(this)
                .load(venue.imageUrl?.thumbnail)
                .into(ivVenue)

        tvVenueName.text = venue.name
    }

    private fun getOldMessages() {
        if (isNetworkActiveWithMessage()) {
            viewModel.getOldMessages()
        }
    }

    private fun sendTextMessage() {
        val message = etMessage.text.toString().trim()
        if (message.isNotBlank() && isNetworkActiveWithMessage()) {
            etMessage.setText("")
            viewModel.sendTextMessage(message)
        }
    }

    private fun showVenueDetails() {
        if (viewModel.isVenueDetailsLoaded()) {
            VenueDetailsActivity.start(this, viewModel.getVenue(), viewModel.getMembers())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.newMessage.removeObserver(newMessageObserver)
        viewModel.oldMessages.removeObserver(oldMessagesObserver)
    }
}