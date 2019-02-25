package com.conversify.ui.profile.settings.blockusers

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.data.local.PrefsManager
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.people.UserCrossedDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.people.details.PeopleDetailsActivity
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.activity_block_users_list.*

class BlockUsersListActivity : BaseActivity(), View.OnClickListener, BlockUsersListAdapter.Callback {

    private val viewModel by lazy { ViewModelProviders.of(this)[BlockUsersListViewModel::class.java] }
    private lateinit var adapter: BlockUsersListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_users_list)

        setupHomeRecycler()
        setListener()
        observeChanges()
    }

    override fun onStart() {
        super.onStart()
        getBlockedUsers()
    }

    private fun setupHomeRecycler() {
        tvNoBlockUser.visibility = View.VISIBLE
        adapter = BlockUsersListAdapter(GlideApp.with(this), this)
        rvBlockUserList.adapter = adapter
    }

    private fun setListener() {
        btnBack.setOnClickListener(this)
        swipeRefreshLayout.setOnRefreshListener { getBlockedUsers() }
    }

    private fun observeChanges() {

        viewModel.blockUsersList.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    swipeRefreshLayout.isRefreshing = false
                    val data = resource.data ?: emptyList()

                    val items = mutableListOf<Any>()
                    items.addAll(data)
                    if (items.size > 0) {
                        tvNoBlockUser.visibility = View.GONE
                    } else tvNoBlockUser.visibility = View.VISIBLE
                    adapter.displayItems(items)
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

    private fun getBlockedUsers() {
        if (isNetworkActiveWithMessage()) {
            viewModel.getBlockedUsers()
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btnBack -> onBackPressed()

        }
    }

    override fun onClick(position: Int, profile: ProfileDto) {
        val items = adapter.getUpdatedList()
        val item = items[position]
        if (item is ProfileDto) {
            val data = UserCrossedDto()
            data.profile = item
            PrefsManager.get().save(PrefsManager.PREF_PEOPLE_USER_ID, item.id!!)
            val intent = PeopleDetailsActivity.getStartIntent(this, data, AppConstants.REQ_CODE_BLOCK_USER)
            startActivity(intent)
        }
    }
}