package com.pulse.ui.venues.join

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.pulse.R
import com.pulse.data.local.PrefsManager
import com.pulse.data.local.UserManager
import com.pulse.data.remote.models.Status
import com.pulse.data.remote.models.chat.MemberDto
import com.pulse.data.remote.models.people.UserCrossedDto
import com.pulse.data.remote.models.venues.VenueDto
import com.pulse.extensions.handleError
import com.pulse.extensions.isNetworkActiveWithMessage
import com.pulse.extensions.longToast
import com.pulse.ui.base.BaseActivity
import com.pulse.ui.custom.LoadingDialog
import com.pulse.ui.people.details.PeopleDetailsActivity
import com.pulse.ui.profile.ProfileActivity
import com.pulse.ui.venues.VenuesViewModel
import com.pulse.utils.AppConstants
import com.pulse.utils.GlideApp
import kotlinx.android.synthetic.main.activity_join_venue.*

class JoinVenueActivity : BaseActivity(), JoinVenueDetailsAdapter.Callback {
    companion object {
        private const val EXTRA_VENUE = "EXTRA_VENUE"

        fun getStartIntent(context: Context, venue: VenueDto): Intent {
            return Intent(context, JoinVenueActivity::class.java)
                    .putExtra(EXTRA_VENUE, venue)
        }
    }

    private val venuesViewModel by lazy { ViewModelProviders.of(this)[VenuesViewModel::class.java] }
    private lateinit var venue: VenueDto
    private lateinit var venueDetailsAdapter: JoinVenueDetailsAdapter
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_venue)

        venue = intent.getParcelableExtra(EXTRA_VENUE)
        loadingDialog = LoadingDialog(this)

        setListeners()
        setupVenueDetailsRecycler(venue)
        observeChanges()
        getVenueDetails()
    }

    private fun setListeners() {
        btnBack.setOnClickListener { onBackPressed() }

        btnJoin.setOnClickListener {
            showAlertDialog()
        }
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(this)
                .setMessage(R.string.join_venue_request_msg)
                .setPositiveButton(R.string.join_venue_request_btn_agree) { _, _ ->
                    if (isNetworkActiveWithMessage()) {
                        venuesViewModel.joinVenue(venue)
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show()
    }

    private fun setupVenueDetailsRecycler(venue: VenueDto) {
        venueDetailsAdapter = JoinVenueDetailsAdapter(GlideApp.with(this), this)
        rvVenueDetails.adapter = venueDetailsAdapter
        venueDetailsAdapter.displayItems(listOf(venue))
    }

    private fun observeChanges() {
        venuesViewModel.venueDetails.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let { venue ->
                        this.venue = venue

                        // Display items in correct order
                        val members = venue.members ?: arrayListOf()
                        val items = mutableListOf<Any>()
                        items.add(venue)
                        items.addAll(members)
                        venueDetailsAdapter.displayItems(items)
                    }
                }

                Status.ERROR -> {
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    // Ignored
                }
            }
        })

        venuesViewModel.joinVenue.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    resource.data?.let { venue ->
                        // Put the updated venue as result
                        val data = Intent()
                        data.putExtra(AppConstants.EXTRA_VENUE, venue)
                        setResult(Activity.RESULT_OK, data)

                        if (venue.isPrivate == true) {
                            // Show message if venue is private
                            longToast(R.string.venues_message_notification_sent_to_admin)
                        } else {
                            // Finish current activity if venue is public and start the venue chat from previous screen
                            finish()
                        }
                    }
                }

                Status.ERROR -> {
                    loadingDialog.setLoading(false)
                    handleError(resource.error)
                }

                Status.LOADING -> loadingDialog.setLoading(true)
            }
        })
    }

    private fun getVenueDetails() {
        if (isNetworkActiveWithMessage()) {
            venuesViewModel.getVenueDetails(venue.id ?: "")
        }
    }

    override fun onMemberClicked(member: MemberDto) {
        val data = UserCrossedDto()
        data.profile = member.user
        PrefsManager.get().save(PrefsManager.PREF_PEOPLE_USER_ID, member.user?.id ?: "")
        if (member.user?.id == UserManager.getUserId()) {
            startActivity(Intent(this, ProfileActivity::class.java))
        } else {
            val intent = PeopleDetailsActivity.getStartIntent(this, data, AppConstants.REQ_CODE_BLOCK_USER)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingDialog.setLoading(false)
    }
}