package com.conversify.ui.chat

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.SimpleItemAnimator
import com.conversify.R
import com.conversify.data.remote.models.chat.ChatMessageDto
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseActivity
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

    private val venue by lazy { intent.getParcelableExtra<VenueDto>(EXTRA_VENUE) }
    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: ChatAdapter

    private val newMessageObserver = Observer<ChatMessageDto> {
        it ?: return@Observer
        setResult(Activity.RESULT_OK)
        adapter.addNewMessage(it)
        rvChat.scrollToPosition(adapter.itemCount - 1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        viewModel = ViewModelProviders.of(this)[ChatViewModel::class.java]
        setListeners()
        observeChanges()
        setupChatRecycler()
        displayToolbarDetails()
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
    }

    private fun setupChatRecycler() {
        swipeRefreshLayout.isEnabled = false
        adapter = ChatAdapter(this, this)
        rvChat.adapter = adapter
        (rvChat.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    private fun displayToolbarDetails() {
        GlideApp.with(this)
                .load(venue.imageUrl?.thumbnail)
                .into(ivVenue)

        tvVenueName.text = venue.name
    }

    private fun sendTextMessage() {
        val message = etMessage.text.toString().trim()
        if (message.isNotBlank() && isNetworkActiveWithMessage()) {
            etMessage.setText("")
            viewModel.sendTextMessage(message)
        }
    }

    private fun showVenueDetails() {
        // todo Open venue details screen
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.newMessage.removeObserver(newMessageObserver)
    }
}