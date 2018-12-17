package com.conversify.ui.creategroup.addparticipants

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.extensions.gone
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.visible
import com.conversify.ui.base.BaseActivity
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.activity_add_participants.*

class AddParticipantsActivity : BaseActivity(), AddParticipantsAdapter.Callback {
    companion object {
        private const val CHILD_FOLLOWERS = 0
        private const val CHILD_NO_FOLLOWERS = 1
    }

    private val viewModel by lazy { ViewModelProviders.of(this)[AddParticipantsViewModel::class.java] }
    private lateinit var adapter: AddParticipantsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_participants)

        swipeRefreshLayout.isEnabled = false
        btnBack.setOnClickListener { onBackPressed() }
        setupParticipantsRecycler()
        observeChanges()
        getFollowers()
    }

    private fun setupParticipantsRecycler() {
        adapter = AddParticipantsAdapter(GlideApp.with(this), this)
        rvParticipants.adapter = adapter
    }

    private fun observeChanges() {
        viewModel.followers.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    swipeRefreshLayout.isRefreshing = false
                    val followers = resource.data ?: emptyList()
                    adapter.displayFollowers(followers)
                    viewSwitcher.displayedChild = if (adapter.itemCount == 0) {
                        CHILD_NO_FOLLOWERS
                    } else {
                        CHILD_FOLLOWERS
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

    override fun onParticipantSelectionChanged() {
        val selectedCount = adapter.getSelectedFollowers().size
        if (selectedCount > 0) {
            btnContinue.visible()
        } else {
            btnContinue.gone()
        }
    }
}