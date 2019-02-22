package com.conversify.ui.profile.settings.hideinfo.hidestatus

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.SearchView
import android.view.View
import com.conversify.R
import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.extensions.handleError
import com.conversify.ui.base.BaseActivity
import com.conversify.utils.GlideApp
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_hide_status.*

class HideStatusActivity : BaseActivity(), View.OnClickListener, HideStatusAdapter.Callback {

    companion object {
        private const val EXTRA_FLAG = "EXTRA_FLAG"
        fun start(context: Context, flag: Int): Intent {
            return Intent(context, HideStatusActivity::class.java)
                    .putExtra(EXTRA_FLAG, flag)
        }
    }

    private val viewModel by lazy { ViewModelProviders.of(this)[HideStatusViewModel::class.java] }
    private var flag = 0
    private lateinit var adapter: HideStatusAdapter
    private lateinit var items: List<Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hide_status)

        flag = intent.getIntExtra(EXTRA_FLAG, 0)
        setData(viewModel.getProfile(), flag)
        setListener()
        observeChanges()
        setupRecyclerView()
        setupSearch()
    }

    private fun setListener() {
        btnBack.setOnClickListener(this)
        everyone.setOnClickListener(this)
        yourFollowers.setOnClickListener(this)
        selectedUser.setOnClickListener(this)
        swipeRefreshLayout.setOnRefreshListener { getFollower() }
    }

    private fun setData(profile: ProfileDto, flag: Int) {

        when (flag) {
            ApiConstants.FLAG_PROFILE_PICTURE -> profilePicture(profile)
            ApiConstants.FLAG_PRIVATE_INFO -> privateInfo(profile)
            ApiConstants.FLAG_USERNAME -> username(profile)
            ApiConstants.FLAG_MESSAGE -> message(profile)
        }

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

    private fun setupRecyclerView() {
        adapter = HideStatusAdapter(GlideApp.with(this), this, viewModel.getProfile(), flag)
        rvUsernameList.adapter = adapter
    }

    private fun profilePicture(profile: ProfileDto) {
        if (profile.imageVisibilityForEveryone!!) {
            everyone.isChecked = profile.imageVisibilityForEveryone
        } else {
            if (profile.imageVisibilityForFollowers!!) {
                val size = profile.imageVisibility?.size
                if (size != 0) {
                    selectedUser.isChecked = true
                } else {
                    yourFollowers.isChecked = profile.imageVisibilityForFollowers
                }
            } else {
                yourFollowers.isChecked = true
            }
        }
    }

    private fun privateInfo(profile: ProfileDto) {
        everyone.visibility = View.GONE
        if (profile.personalInfoVisibilityForFollowers!!) {
            val size = profile.personalInfoVisibility?.size
            if (size != 0) {
                selectedUser.isChecked = true
            } else {
                yourFollowers.isChecked = profile.personalInfoVisibilityForFollowers
            }
        } else {
            yourFollowers.isChecked = true
        }
    }

    private fun username(profile: ProfileDto) {
        if (profile.nameVisibilityForEveryone!!) {
            everyone.isChecked = profile.nameVisibilityForEveryone
        } else {
            if (profile.nameVisibilityForFollowers!!) {
                val size = profile.nameVisibility?.size
                if (size != 0) {
                    selectedUser.isChecked = true
                } else {
                    yourFollowers.isChecked = profile.nameVisibilityForFollowers
                }
            } else {
                yourFollowers.isChecked = true
            }
        }
    }

    private fun message(profile: ProfileDto) {
        if (profile.tagPermissionForEveryone!!) {
            everyone.isChecked = profile.tagPermissionForEveryone
        } else {
            if (profile.tagPermissionForFollowers!!) {
                val size = profile.tagPermission?.size
                if (size != 0) {
                    selectedUser.isChecked = true
                } else {
                    yourFollowers.isChecked = profile.tagPermissionForFollowers
                }
            } else {
                yourFollowers.isChecked = true
            }
        }
    }

    private fun getFollower() {
        viewModel.getFollowerList()
    }

    private fun observeChanges() {

        viewModel.configSetting.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    val data = resource.data

                }

                Status.ERROR -> {
//                    handleError(resource.error)
                }

                Status.LOADING -> {
                }
            }
        })

        viewModel.followerList.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    swipeRefreshLayout.isRefreshing = false
                    items = resource.data ?: emptyList()
                    adapter.displayCategories(items)
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

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btnBack -> onBackPressed()

            R.id.everyone -> {
            }

            R.id.yourFollowers -> {
            }

            R.id.selectedUser -> getFollower()
        }
    }

    override fun onClick(position: Int) {
        val item = items[position]
        adapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val map = hashMapOf<String, String>()
        map["flag"] = flag.toString()
        when (rgStatus.checkedRadioButtonId) {
            R.id.everyone -> {
                when (flag) {
                    ApiConstants.FLAG_PROFILE_PICTURE -> {
                        map["imageVisibilityForEveryone"] = true.toString()
                    }
                    ApiConstants.FLAG_USERNAME -> {
                        map["nameVisibilityForEveryone"] = true.toString()
                    }
                    ApiConstants.FLAG_MESSAGE -> {
                        map["tagPermissionForEveryone"] = true.toString()
                    }
                }
            }

            R.id.yourFollowers -> {
                when (flag) {
                    ApiConstants.FLAG_PROFILE_PICTURE -> {
                        map["imageVisibilityForFollowers"] = true.toString()
                    }
                    ApiConstants.FLAG_PRIVATE_INFO -> {
                        map["personalInfoVisibilityForFollowers"] = true.toString()
                    }
                    ApiConstants.FLAG_USERNAME -> {
                        map["nameVisibilityForFollowers"] = true.toString()
                    }
                    ApiConstants.FLAG_MESSAGE -> {
                        map["tagPermissionForFollowers"] = true.toString()
                    }
                }
            }

            R.id.selectedUser -> {
                when (flag) {
                    ApiConstants.FLAG_PROFILE_PICTURE -> {
                        val array = Gson().toJson("")
                        map["userIds"] = array
                    }
                    ApiConstants.FLAG_PRIVATE_INFO -> {
                        map["userIds"] = true.toString()
                    }
                    ApiConstants.FLAG_USERNAME -> {
                        map["userIds"] = true.toString()
                    }
                    ApiConstants.FLAG_MESSAGE -> {
                        map["userIds"] = true.toString()
                    }
                }
            }
        }
        viewModel.configSetting(map)
    }
}