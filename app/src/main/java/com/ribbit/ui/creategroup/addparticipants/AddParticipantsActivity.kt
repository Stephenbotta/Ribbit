package com.ribbit.ui.creategroup.addparticipants

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ribbit.R
import com.ribbit.data.remote.models.Status
import com.ribbit.extensions.gone
import com.ribbit.extensions.handleError
import com.ribbit.extensions.isNetworkActiveWithMessage
import com.ribbit.extensions.visible
import com.ribbit.ui.base.BaseActivity
import com.ribbit.utils.AppConstants
import com.ribbit.utils.GlideApp
import kotlinx.android.synthetic.main.activity_add_participants.*

class AddParticipantsActivity : BaseActivity() {
    companion object {
        private const val CHILD_FOLLOWERS = 0
        private const val CHILD_NO_FOLLOWERS = 1

        private const val EXTRA_PARTICIPANT_IDS = "EXTRA_PARTICIPANT_IDS"

        fun getStartIntent(context: Context, participantIds: ArrayList<String>): Intent {
            val intent = Intent(context, AddParticipantsActivity::class.java)
            intent.putStringArrayListExtra(EXTRA_PARTICIPANT_IDS, participantIds)
            return intent
        }
    }

    private val viewModel by lazy { ViewModelProviders.of(this)[AddParticipantsViewModel::class.java] }
    private val participantIds by lazy { intent.getStringArrayListExtra(EXTRA_PARTICIPANT_IDS) }
    private lateinit var adapter: AddParticipantsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_participants)

        swipeRefreshLayout.isEnabled = false
        setListeners()
        setupParticipantsRecycler()
        observeChanges()
        getFollowers()
    }

    private fun setListeners() {
        btnBack.setOnClickListener { onBackPressed() }
        btnContinue.setOnClickListener {
            val participants = adapter.getSelectedFollowers()
            val data = Intent()
            data.putParcelableArrayListExtra(AppConstants.EXTRA_PARTICIPANTS, participants)
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    private fun setupParticipantsRecycler() {
        adapter = AddParticipantsAdapter(GlideApp.with(this))
        rvParticipants.adapter = adapter
    }

    private fun observeChanges() {
        viewModel.followers.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    swipeRefreshLayout.isRefreshing = false
                    val followers = resource.data ?: emptyList()
                    followers.forEach {
                        // Select the previously selected participant ids
                        if (participantIds.contains(it.id)) {
                            it.isSelected = true
                        }
                    }
                    adapter.displayFollowers(followers)
                    if (adapter.itemCount == 0) {
                        viewSwitcher.displayedChild = CHILD_NO_FOLLOWERS
                        btnContinue.gone()
                    } else {
                        viewSwitcher.displayedChild = CHILD_FOLLOWERS
                        btnContinue.visible()
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
    }

    private fun getFollowers() {
        if (isNetworkActiveWithMessage()) {
            viewModel.getFollowers()
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }
}