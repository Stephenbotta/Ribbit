package com.conversify.ui.profile.settings.hideinfo.hidestatus

import android.app.Activity
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
import com.conversify.data.remote.models.loginsignup.SelectedUser
import com.conversify.extensions.handleError
import com.conversify.ui.base.BaseActivity
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
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
    private var items= listOf<Any>()
    private val selectedUserList by lazy { ArrayList<String>() }

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
            AppConstants.REQ_CODE_NEW_POST -> newPost(intent.getStringArrayListExtra(AppConstants.EXTRA_FOLLOWERS))
        }

        if (selectedUser.isChecked)
            getFollower()
    }

    private fun newPost(list: ArrayList<String>) {
        everyone.visibility = View.GONE
        if (list.size > 0) {
            selectedUser.isChecked = true
        } else {
            yourFollowers.isChecked = true
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
        if (profile.imageVisibilityForEveryone == true) {
            everyone.isChecked = profile.imageVisibilityForEveryone
        } else {
            if (profile.imageVisibilityForFollowers == false) {
                selectedUser.isChecked = true
            } else {
                yourFollowers.isChecked = true
            }
        }
    }

    private fun privateInfo(profile: ProfileDto) {
        everyone.visibility = View.GONE
        if (profile.personalInfoVisibilityForFollowers == false) {
            selectedUser.isChecked = true
        } else {
            yourFollowers.isChecked = true
        }
    }

    private fun username(profile: ProfileDto) {
        if (profile.nameVisibilityForEveryone == true) {
            everyone.isChecked = profile.nameVisibilityForEveryone
        } else {
            if (profile.nameVisibilityForFollowers == false) {
                selectedUser.isChecked = true
            } else {
                yourFollowers.isChecked = true
            }
        }
    }

    private fun message(profile: ProfileDto) {
        if (profile.tagPermissionForEveryone == true) {
            everyone.isChecked = profile.tagPermissionForEveryone
        } else {
            if (profile.tagPermissionForFollowers == false) {
                selectedUser.isChecked = true
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
                    val data = Intent()
                    data.putExtra(AppConstants.EXTRA_PROFILE, resource.data)
                    setResult(Activity.RESULT_OK, data)
                    super.onBackPressed()
                }

                Status.ERROR -> {
//                    handleError(resource.error)
                    super.onBackPressed()
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
                    for (index in items.indices) {
                        when (flag) {
                            ApiConstants.FLAG_PROFILE_PICTURE -> {
                                selectedUser(viewModel.getProfile().imageVisibility
                                        ?: emptyList(), index)
                            }
                            ApiConstants.FLAG_PRIVATE_INFO -> {
                                selectedUser(viewModel.getProfile().personalInfoVisibility
                                        ?: emptyList(), index)
                            }
                            ApiConstants.FLAG_USERNAME -> {
                                selectedUser(viewModel.getProfile().nameVisibility
                                        ?: emptyList(), index)
                            }
                            ApiConstants.FLAG_MESSAGE -> {
                                selectedUser(viewModel.getProfile().tagPermission
                                        ?: emptyList(), index)
                            }
                            AppConstants.REQ_CODE_NEW_POST -> {
                                selectedUserPost(intent.getStringArrayListExtra(AppConstants.EXTRA_FOLLOWERS), index)
                            }
                        }

                    }
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

    private fun selectedUser(list: List<SelectedUser>, index: Int) {
        for (selected in list.indices) {
            val item = items[index]
            if (item is ProfileDto) {
                if (item.id.equals(list[selected].id)) {
                    item.isSelected = true
                    break
                }
            }
        }
    }

    private fun selectedUserPost(list: MutableList<String>, index: Int) {
        for (selected in list.indices) {
            val item = items[index]
            if (item is ProfileDto) {
                if (item.id.equals(list[selected])) {
                    item.isSelected = true
                    break
                }
            }
        }
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
        if (item is ProfileDto) {
            item.isSelected = item.isSelected.not()
        }
        adapter.notifyDataSetChanged()
    }

    private fun onBackPress() {
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
                viewModel.configSetting(map)
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
                viewModel.configSetting(map)
            }

            R.id.selectedUser -> {
                buildIdArray()
                val list = selectedUserList.toList()
                viewModel.configSetting(flag, list)
            }
        }
    }

    private fun buildIdArray() {
        for (i in items.indices) {
            val item = items[i]
            if (item is ProfileDto) {
                if (item.isSelected) {
                    selectedUserList.add(item.id ?: "")
                }
            }
        }
    }

    override fun onBackPressed() {
        when (flag) {
            AppConstants.REQ_CODE_NEW_POST -> {
                buildIdArray()
                val data = Intent()
                if (yourFollowers.isChecked)
                    selectedUserList.clear()
                data.putStringArrayListExtra(AppConstants.EXTRA_FOLLOWERS, selectedUserList)
                setResult(Activity.RESULT_OK, data)
                finish()
            }
            else -> onBackPress()
        }
    }
}