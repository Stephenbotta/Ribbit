package com.conversify.ui.profile.followerandfollowing

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.SearchView
import android.view.View
import com.conversify.R
import com.conversify.data.local.PrefsManager
import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.people.UserCrossedDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.people.details.PeopleDetailsActivity
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.activity_follower_and_following.*

class FollowerAndFollowingActivity : BaseActivity(), View.OnClickListener, FollowerAndFollowingAdapter.Callback {

    companion object {
        private const val EXTRA_FLAG = "EXTRA_FLAG"
        fun getIntentStart(context: Context, flag: Int): Intent {
            return Intent(context, FollowerAndFollowingActivity::class.java)
                    .putExtra(EXTRA_FLAG, flag)
        }
    }


    private val viewModel by lazy { ViewModelProviders.of(this)[FollowerAndFollowingViewModel::class.java] }
    private lateinit var adapter: FollowerAndFollowingAdapter
    private var flag = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follower_and_following)

        flag = intent.getIntExtra(EXTRA_FLAG, 0)
        setupHomeRecycler()
        setListener()
        observeChanges()
        setupSearch()
    }

    override fun onStart() {
        super.onStart()
        getUsers()
    }

    private fun setupHomeRecycler() {
        when (flag) {
            ApiConstants.FLAG_FOLLOWERS -> tvUser.text = getString(R.string.follower_label_empty_list)
            ApiConstants.FLAG_FOLLOWINGS -> tvUser.text = getString(R.string.following_label_empty_list)
        }
        tvUser.visibility = View.VISIBLE
        adapter = FollowerAndFollowingAdapter(GlideApp.with(this), this)
        rvUserList.adapter = adapter
    }

    private fun setupSearch() {
        searchUser.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String?): Boolean {
                viewModel.searchUsername(query ?: "")
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        })
    }

    private fun setListener() {
        btnBack.setOnClickListener(this)
        swipeRefreshLayout.setOnRefreshListener { getUsers() }
    }

    private fun observeChanges() {

        viewModel.followerList.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    swipeRefreshLayout.isRefreshing = false
                    val data = resource.data ?: emptyList()

                    val items = mutableListOf<Any>()
                    items.addAll(data)
                    if (items.size > 0) {
                        tvUser.visibility = View.GONE
                    } else tvUser.visibility = View.VISIBLE
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

    private fun getUsers() {
        if (isNetworkActiveWithMessage()) {
            viewModel.getUsers(flag)
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
            PrefsManager.get().save(PrefsManager.PREF_PEOPLE_USER_ID, item.id?:"")
            val intent = PeopleDetailsActivity.getStartIntent(this, data, AppConstants.REQ_CODE_BLOCK_USER)
            startActivity(intent)
        }
    }
}