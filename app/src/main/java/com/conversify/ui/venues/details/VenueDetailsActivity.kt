package com.conversify.ui.venues.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.view.Menu
import android.view.MenuItem
import com.conversify.R
import com.conversify.data.remote.models.chat.VenueMemberDto
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.ui.base.BaseActivity
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.activity_venue_details.*

class VenueDetailsActivity : BaseActivity(), VenueDetailsAdapter.Callback {
    companion object {
        private const val EXTRA_VENUE = "EXTRA_VENUE"
        private const val EXTRA_VENUE_MEMBERS = "EXTRA_VENUE_MEMBERS"

        fun start(context: Context, venue: VenueDto, members: ArrayList<VenueMemberDto>) {
            context.startActivity(Intent(context, VenueDetailsActivity::class.java)
                    .putExtra(EXTRA_VENUE, venue)
                    .putExtra(EXTRA_VENUE_MEMBERS, members))
        }
    }

    private val venue by lazy { intent.getParcelableExtra<VenueDto>(EXTRA_VENUE) }
    private val members by lazy { intent.getParcelableArrayListExtra<VenueMemberDto>(EXTRA_VENUE_MEMBERS) }
    private lateinit var venueDetailsAdapter: VenueDetailsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venue_details)

        setupToolbar()
        setupVenueDetailsRecycler()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_white)
        }

        val boldTypeface = ResourcesCompat.getFont(this, R.font.brandon_text_bold)
        collapsingToolbar.setExpandedTitleTypeface(boldTypeface)
        collapsingToolbar.setCollapsedTitleTypeface(boldTypeface)
        collapsingToolbar.title = venue.name

        val thumbnail = GlideApp.with(this).load(venue.imageUrl?.thumbnail)
        GlideApp.with(this)
                .load(venue.imageUrl?.original)
                .thumbnail(thumbnail)
                .into(ivVenue)
    }

    private fun setupVenueDetailsRecycler() {
        venueDetailsAdapter = VenueDetailsAdapter(GlideApp.with(this), this)
        rvVenueDetails.adapter = venueDetailsAdapter

        val items = mutableListOf<Any>()
        items.add(venue)
        items.addAll(members)
        items.add(Any())

        venueDetailsAdapter.displayItems(items)
    }

    override fun onNotificationChanged(isEnabled: Boolean) {
    }

    override fun onMemberClicked(member: VenueMemberDto) {
    }

    override fun onExitGroupClicked() {
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_venue_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            R.id.menuShare -> {
                true
            }

            R.id.menuMore -> {
                true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
}