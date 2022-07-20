package com.ribbit.ui.profile.settings.hideinfo.hidestatus

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ribbit.R
import com.ribbit.data.remote.ApiConstants
import com.ribbit.data.remote.models.Status
import com.ribbit.ui.loginsignup.ProfileDto
import com.ribbit.data.remote.models.loginsignup.SelectedUser
import com.ribbit.extensions.handleError
import com.ribbit.ui.base.BaseActivity
import com.ribbit.utils.AppConstants
import com.ribbit.utils.GlideApp
import kotlinx.android.synthetic.main.activity_hide_status.*

class HideStatusActivity : BaseActivity(), View.OnClickListener {

    companion object {
        private const val EXTRA_FLAG = "EXTRA_FLAG"
        fun start(context: Context, flag: Int): Intent {
            return Intent(context, HideStatusActivity::class.java)
                    .putExtra(EXTRA_FLAG, flag)
        }
    }

    private val viewModel by lazy { ViewModelProviders.of(this)[HideStatusViewModel::class.java] }
    private val flag by lazy { intent.getIntExtra(EXTRA_FLAG, 0) }
    private lateinit var adapter: HideStatusAdapter
    override fun onSavedInstance(outState: Bundle?, outPersisent: PersistableBundle?) {
        TODO("Not yet implemented")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hide_status)

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
            AppConstants.REQ_CODE_NEW_POST -> intent.getStringArrayListExtra(AppConstants.EXTRA_FOLLOWERS)
                ?.let { newPost(it) }
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
        adapter = HideStatusAdapter(GlideApp.with(this))
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
                    val users = resource.data ?: emptyList()
                    for (user in users) {
                        when (flag) {
                            ApiConstants.FLAG_PROFILE_PICTURE -> {
                                selectedUser(viewModel.getProfile().imageVisibility
                                        ?: emptyList(), user)
                            }
                            ApiConstants.FLAG_PRIVATE_INFO -> {
                                selectedUser(viewModel.getProfile().personalInfoVisibility
                                        ?: emptyList(), user)
                            }
                            ApiConstants.FLAG_USERNAME -> {
                                selectedUser(viewModel.getProfile().nameVisibility
                                        ?: emptyList(), user)
                            }
                            ApiConstants.FLAG_MESSAGE -> {
                                selectedUser(viewModel.getProfile().tagPermission
                                        ?: emptyList(), user)
                            }
                            AppConstants.REQ_CODE_NEW_POST -> {
                                intent.getStringArrayListExtra(AppConstants.EXTRA_FOLLOWERS)
                                    ?.let { selectedUserPost(it, user) }
                            }
                        }

                    }
                    adapter.displayCategories(users)
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

    private fun selectedUser(usersList: List<SelectedUser>, user: ProfileDto) {
        for (userItem in usersList) {
            if (user.id == userItem.id) {
                user.isSelected = true
                break
            }
        }
    }

    private fun selectedUserPost(users: MutableList<String>, user: ProfileDto) {
        for (userItem in users) {
            if (user.id == userItem) {
                user.isSelected = true
                break
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {

            R.id.btnBack -> onBackPressed()

            R.id.everyone -> {
                adapter.displayCategories(arrayListOf())
            }

            R.id.yourFollowers -> {
                adapter.displayCategories(arrayListOf())
            }

            R.id.selectedUser -> getFollower()
        }
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
                viewModel.configSetting(flag, adapter.getSelectedUserIds())
            }
        }
    }

    override fun onBackPressed() {
        when (flag) {
            AppConstants.REQ_CODE_NEW_POST -> {
                val data = Intent()
                val selectedUserList = ArrayList<String>()
                if (!yourFollowers.isChecked)
                    selectedUserList.addAll(adapter.getSelectedUserIds())
                data.putStringArrayListExtra(AppConstants.EXTRA_FOLLOWERS, selectedUserList)
                setResult(Activity.RESULT_OK, data)
                finish()
            }
            else -> onBackPress()
        }
    }
}