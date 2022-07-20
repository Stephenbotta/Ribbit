package com.ribbit.ui.profile.settings.blockusers

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ribbit.R
import com.ribbit.data.remote.models.Status
import com.ribbit.ui.loginsignup.ProfileDto
import com.ribbit.data.remote.models.people.UserCrossedDto
import com.ribbit.extensions.gone
import com.ribbit.extensions.handleError
import com.ribbit.extensions.isNetworkActiveWithMessage
import com.ribbit.extensions.visible
import com.ribbit.ui.base.BaseActivity
import com.ribbit.ui.people.details.PeopleDetailsActivity
import com.ribbit.utils.AppConstants
import com.ribbit.utils.GlideApp
import kotlinx.android.synthetic.main.activity_block_users_list.*

class BlockUsersListActivity : BaseActivity(), View.OnClickListener, BlockUsersListAdapter.Callback {

    private val viewModel by lazy { ViewModelProviders.of(this)[BlockUsersListViewModel::class.java] }
    private lateinit var adapter: BlockUsersListAdapter
    override fun onSavedInstance(outState: Bundle?, outPersisent: PersistableBundle?) {
        TODO("Not yet implemented")
    }

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
                    if (data.isNotEmpty()) {
                        tvNoBlockUser.gone()
                    } else tvNoBlockUser.visible()
                    adapter.displayItems(data)
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

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnBack -> onBackPressed()
        }
    }

    override fun onClick(profile: ProfileDto) {
        val data = UserCrossedDto()
        data.profile = profile
        val intent = PeopleDetailsActivity.getStartIntent(this, data,
                AppConstants.REQ_CODE_BLOCK_USER, data.profile?.id ?: "")
        startActivity(intent)
    }
}