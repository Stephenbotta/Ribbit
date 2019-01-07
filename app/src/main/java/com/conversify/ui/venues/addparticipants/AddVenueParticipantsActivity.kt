package com.conversify.ui.venues.addparticipants

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.extensions.*
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.creategroup.addparticipants.AddParticipantsActivity
import com.conversify.ui.creategroup.addparticipants.AddParticipantsAdapter
import com.conversify.ui.creategroup.addparticipants.ParticipantViewHolder
import com.conversify.ui.custom.LoadingDialog
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.activity_add_participants.*

class AddVenueParticipantsActivity : BaseActivity() {
    companion object {
        private const val CHILD_FOLLOWERS = 0
        private const val CHILD_NO_FOLLOWERS = 1

        private const val EXTRA_VENUE_ID = "EXTRA_VENUE_ID"

        fun start(context: Context, venueId: String) {
            context.startActivity(Intent(context, AddVenueParticipantsActivity::class.java)
                    .putExtra(EXTRA_VENUE_ID, venueId))
        }
    }

    private val viewModel by lazy { ViewModelProviders.of(this)[AddVenueParticipantsViewModel::class.java] }
    private val selectedParticipantIds by lazy { mutableSetOf<String>() }
    private lateinit var venueId: String
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var addParticipantsAdapter: AddParticipantsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_participants)

        venueId = intent.getStringExtra(EXTRA_VENUE_ID)
        loadingDialog = LoadingDialog(this)
        swipeRefreshLayout.isEnabled = false

        setListeners()
        setupParticipantsRecycler()
        observeChanges()
        getFollowers()
    }

    private fun setListeners() {
        btnBack.setOnClickListener { onBackPressed() }
        btnContinue.setOnClickListener {
            if (isNetworkActiveWithMessage()) {
                viewModel.addVenueParticipants(venueId, selectedParticipantIds.toList())
            }
        }
    }

    private fun setupParticipantsRecycler() {
        addParticipantsAdapter = AddParticipantsAdapter(GlideApp.with(this))
        addParticipantsAdapter.setCallback(object : ParticipantViewHolder.Callback {
            override fun onParticipantClicked(profile: ProfileDto) {
                val profileId = profile.id ?: return
                if (profile.isSelected) {
                    selectedParticipantIds.add(profileId)
                } else {
                    selectedParticipantIds.remove(profileId)
                }
                if (selectedParticipantIds.isEmpty()) {
                    btnContinue.gone()
                } else {
                    btnContinue.visible()
                }
            }
        })
        rvParticipants.adapter = addParticipantsAdapter
    }

    private fun observeChanges() {
        viewModel.getVenueAddParticipants.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    swipeRefreshLayout.isRefreshing = false
                    val participants = resource.data ?: emptyList()
                    addParticipantsAdapter.displayFollowers(participants)

                    if (addParticipantsAdapter.itemCount == 0) {
                        viewSwitcher.displayedChild = CHILD_NO_FOLLOWERS
                    } else {
                        viewSwitcher.displayedChild = CHILD_FOLLOWERS
                    }
                }

                Status.ERROR -> {
                    swipeRefreshLayout.isRefreshing = false
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    swipeRefreshLayout.isRefreshing = true
                }
            }
        })

        viewModel.addVenueParticipants.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    longToast(R.string.add_venue_participants_message_request_sent)
                    finish()
                }

                Status.ERROR -> {
                    loadingDialog.setLoading(false)
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    loadingDialog.setLoading(true)
                }
            }
        })
    }

    private fun getFollowers() {
        if (isNetworkActiveWithMessage()) {
            viewModel.getVenueAddParticipants(venueId)
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingDialog.setLoading(false)
    }
}