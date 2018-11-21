package com.conversify.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.conversify.R
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.ui.base.BaseActivity
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : BaseActivity() {
    companion object {
        private const val EXTRA_VENUE = "EXTRA_VENUE"

        fun start(context: Context, venue: VenueDto) {
            context.startActivity(Intent(context, ChatActivity::class.java)
                    .putExtra(EXTRA_VENUE, venue))
        }
    }

    private val venue by lazy { intent.getParcelableExtra<VenueDto>(EXTRA_VENUE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        toolbar.setNavigationOnClickListener { onBackPressed() }

        GlideApp.with(this)
                .load(venue.imageUrl?.thumbnail)
                .into(ivVenue)

        tvVenueName.text = venue.name
    }
}