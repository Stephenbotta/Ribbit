package com.conversify.ui.venues.details

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import com.conversify.R
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.chat.VenueMemberDto
import com.conversify.data.remote.models.groups.AddParticipantsDto
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.custom.LoadingDialog
import com.conversify.ui.venues.addparticipants.AddVenueParticipantsActivity
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.activity_venue_details.*

class VenueDetailsActivity : BaseActivity(), VenueDetailsAdapter.Callback {
    companion object {
        private const val EXTRA_VENUE = "EXTRA_VENUE"
        private const val EXTRA_VENUE_MEMBERS = "EXTRA_VENUE_MEMBERS"

        fun getStartIntent(context: Context, venue: VenueDto, members: ArrayList<VenueMemberDto>): Intent {
            return Intent(context, VenueDetailsActivity::class.java)
                    .putExtra(EXTRA_VENUE, venue)
                    .putExtra(EXTRA_VENUE_MEMBERS, members)
        }
    }

    private val venue by lazy { intent.getParcelableExtra<VenueDto>(EXTRA_VENUE) }
    private val members by lazy { intent.getParcelableArrayListExtra<VenueMemberDto>(EXTRA_VENUE_MEMBERS) }
    private lateinit var viewModel: VenueDetailsViewModel
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venue_details)

        viewModel = ViewModelProviders.of(this)[VenueDetailsViewModel::class.java]
        loadingDialog = LoadingDialog(this)
        setupToolbar()
        observeChanges()
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

    private fun observeChanges() {
        viewModel.changeVenueNotifications.observe(this, Observer { resource ->
            resource ?: return@Observer
            when (resource.status) {
                Status.SUCCESS -> {
                    venue.notification = resource.data
                    val data = Intent()
                    data.putExtra(AppConstants.EXTRA_VENUE, venue)
                    setResult(Activity.RESULT_OK, data)
                }

                Status.ERROR -> {
                    handleError(resource.error)
                }

                Status.LOADING -> {
                }
            }
        })

        val exitOrArchiveVenueObserver = Observer<Resource<Any>> { resource ->
            resource ?: return@Observer
            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    venue.isMember = false
                    val data = Intent()
                    data.putExtra(AppConstants.EXTRA_VENUE, venue)
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }

                Status.ERROR -> {
                    handleError(resource.error)
                    loadingDialog.setLoading(false)
                }

                Status.LOADING -> {
                    loadingDialog.setLoading(true)
                }
            }
        }

        viewModel.exitVenue.observe(this, exitOrArchiveVenueObserver)
        viewModel.archiveVenue.observe(this, exitOrArchiveVenueObserver)
    }

    private fun setupVenueDetailsRecycler() {
        val venueDetailsAdapter = VenueDetailsAdapter(GlideApp.with(this), this)
        rvVenueDetails.adapter = venueDetailsAdapter

        val items = mutableListOf<Any>()
        items.add(venue)    // Header
        items.add(AddParticipantsDto)    // Add participants
        items.addAll(members)   // Members
        items.add(Any())    // Exit group

        venueDetailsAdapter.displayItems(items)
    }

    override fun onNotificationsChanged(isEnabled: Boolean) {
        viewModel.changeVenueNotifications(venue.id ?: "", isEnabled)
    }

    override fun onAddParticipantsClicked() {
        AddVenueParticipantsActivity.start(this, venue.id ?: "")
    }

    override fun onMemberClicked(member: VenueMemberDto) {
    }

    override fun onExitVenueClicked() {
        AlertDialog.Builder(this)
                .setMessage(R.string.venue_details_label_exit_venue_question)
                .setPositiveButton(R.string.venue_details_btn_exit) { _, _ ->
                    if (isNetworkActiveWithMessage()) {
                        viewModel.exitVenue(venue.id ?: "")
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show()
    }

    override fun onArchiveVenueClicked() {
        AlertDialog.Builder(this)
                .setMessage(R.string.venue_details_label_archive_venue_question)
                .setPositiveButton(R.string.venue_details_btn_archive) { _, _ ->
                    if (isNetworkActiveWithMessage()) {
                        viewModel.archiveVenue(venue.id ?: "")
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show()
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

    override fun onDestroy() {
        super.onDestroy()
        loadingDialog.setLoading(false)
    }
}