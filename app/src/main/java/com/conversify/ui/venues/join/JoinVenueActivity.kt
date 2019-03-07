package com.conversify.ui.venues.join

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.conversify.R
import com.conversify.data.local.PrefsManager
import com.conversify.data.local.UserManager
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.chat.MemberDto
import com.conversify.data.remote.models.people.UserCrossedDto
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.longToast
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.custom.LoadingDialog
import com.conversify.ui.people.details.PeopleDetailsActivity
import com.conversify.ui.profile.ProfileActivity
import com.conversify.ui.venues.VenuesViewModel
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
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
            if (isNetworkActiveWithMessage()) {
                venuesViewModel.joinVenue(venue)
            }
        }
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