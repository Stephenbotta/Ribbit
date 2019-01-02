package com.conversify.ui.venues.join

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.conversify.R
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.venues.details.VenueDetailsActivity
import com.conversify.ui.venues.details.VenueDetailsAdapter

class JoinVenueActivity : BaseActivity() {
    companion object {
        private const val EXTRA_VENUE = "EXTRA_VENUE"

        fun getStartIntent(context: Context, venue: VenueDto): Intent {
            return Intent(context, VenueDetailsActivity::class.java)
                    .putExtra(EXTRA_VENUE, venue)
        }
    }

    private lateinit var venueDetailsAdapter: VenueDetailsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_venue)
    }
}