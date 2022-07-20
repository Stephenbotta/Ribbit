package com.ribbit.ui.profile.followerandfollowing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ribbit.R
import com.ribbit.data.local.UserManager
import com.ribbit.data.remote.ApiConstants
import com.ribbit.data.remote.models.Status
import com.ribbit.ui.loginsignup.ProfileDto
import com.ribbit.data.remote.models.people.UserCrossedDto
import com.ribbit.extensions.gone
import com.ribbit.extensions.handleError
import com.ribbit.extensions.isNetworkActiveWithMessage
import com.ribbit.extensions.visible
import com.ribbit.ui.base.BaseActivity
import com.ribbit.ui.people.details.PeopleDetailsActivity
import com.ribbit.ui.profile.ProfileActivity
import com.ribbit.utils.AppConstants
import com.ribbit.utils.GlideApp
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
    override fun onSavedInstance(outState: Bundle?, outPersisent: PersistableBundle?) {
        TODO("Not yet implemented")
    }

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
            AppConstants.REQ_CODE_POST_LIKE -> tvUser.text = getString(R.string.post_like_label_empty_list)
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
                    if (data.isNotEmpty()) tvUser.gone() else tvUser.visible()
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

    private fun getUsers() {
        if (isNetworkActiveWithMessage()) {
            when (flag) {
                ApiConstants.FLAG_FOLLOWERS, ApiConstants.FLAG_FOLLOWINGS -> {
                    viewModel.getUsers(flag)
                }
                AppConstants.REQ_CODE_POST_LIKE -> {
                    intent.getStringExtra(AppConstants.EXTRA_POST_ID)
                        ?.let { viewModel.getLikeUserList(it) }
                }
            }
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
        if (profile.id == UserManager.getUserId()) {
            startActivity(Intent(this, ProfileActivity::class.java))
        } else {
            val intent = PeopleDetailsActivity.getStartIntent(this, data,
                    AppConstants.REQ_CODE_BLOCK_USER, data.profile?.id ?: "")
            startActivity(intent)
        }
    }
}