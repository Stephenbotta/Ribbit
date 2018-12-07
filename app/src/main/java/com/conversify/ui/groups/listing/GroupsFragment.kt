package com.conversify.ui.groups.listing

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.SearchView
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.loginsignup.InterestDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.longToast
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.creategroup.CreateGroupActivity
import com.conversify.ui.custom.LoadingDialog
import com.conversify.ui.groups.topicgroups.TopicGroupsActivity
import com.conversify.ui.groups.topics.GroupTopicsFragment
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import com.leinardi.android.speeddial.SpeedDialActionItem
import kotlinx.android.synthetic.main.fragment_groups.*

class GroupsFragment : BaseFragment(), GroupsAdapter.Callback {
    companion object {
        const val TAG = "GroupsFragment"

        private const val CHILD_GROUPS = 0
        private const val CHILD_NO_GROUPS = 1
    }

    private lateinit var viewModel: GroupsViewModel
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var groupsAdapter: GroupsAdapter

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_groups

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)[GroupsViewModel::class.java]
        loadingDialog = LoadingDialog(requireActivity())
        groupsAdapter = GroupsAdapter(GlideApp.with(this), this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGroupsRecycler()
        setupGroupsFab()
        observeChanges()
        getGroups()
    }

    private fun setupGroupsRecycler() {
        rvGroups.adapter = groupsAdapter

        swipeRefreshLayout.setOnRefreshListener { getGroups() }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String?): Boolean {
                viewModel.searchGroups(query ?: "")
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        })
    }

    private fun setupGroupsFab() {
        val colorWhite = ContextCompat.getColor(requireActivity(), R.color.white)
        val colorPrimary = ContextCompat.getColor(requireActivity(), R.color.colorPrimary)

        fabGroups.addActionItem(SpeedDialActionItem.Builder(R.id.fabAddGroup, R.drawable.ic_plus_white)
                .setFabBackgroundColor(colorWhite)
                .setFabImageTintColor(colorPrimary)
                .setLabel(R.string.groups_label_add_new_group)
                .setLabelColor(colorPrimary)
                .create())

        fabGroups.addActionItem(SpeedDialActionItem.Builder(R.id.fabTopics, R.drawable.ic_grid)
                .setFabBackgroundColor(colorWhite)
                .setFabImageTintColor(colorPrimary)
                .setLabel(R.string.groups_label_topics)
                .setLabelColor(colorPrimary)
                .create())

        fabGroups.setOnActionSelectedListener { item ->
            return@setOnActionSelectedListener when (item.id) {
                R.id.fabAddGroup -> {
                    val intent = Intent(requireActivity(), CreateGroupActivity::class.java)
                    startActivityForResult(intent, AppConstants.REQ_CODE_CREATE_GROUP)
                    false
                }

                R.id.fabTopics -> {
                    val fragment = GroupTopicsFragment()
                    fragment.setTargetFragment(this, AppConstants.REQ_CODE_GROUP_TOPIC)
                    fragment.show(fragmentManager, GroupTopicsFragment.TAG)
                    false
                }

                else -> false
            }
        }
    }

    private fun observeChanges() {
        viewModel.groups.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    val items = resource.data ?: emptyList()
                    groupsAdapter.displayItems(items)
                    swipeRefreshLayout.isRefreshing = false

                    viewSwitcher.displayedChild = if (groupsAdapter.itemCount == 0) {
                        CHILD_NO_GROUPS
                    } else {
                        CHILD_GROUPS
                    }
                }

                Status.ERROR -> {
                    handleError(resource.error)
                    swipeRefreshLayout.isRefreshing = false
                }

                Status.LOADING -> swipeRefreshLayout.isRefreshing = true
            }
        })

        viewModel.joinGroup.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)

                    resource.data?.let { venue ->
                        if (venue.isPrivate == true) {
                            requireActivity().longToast(R.string.venues_message_notification_sent_to_admin)
                        } else {
                            getGroups(false)
                        }
                    }
                }

                Status.ERROR -> {
                    loadingDialog.setLoading(false)
                    handleError(resource.error)
                }

                Status.LOADING -> loadingDialog.setLoading(true)
            }
        })
    }

    private fun getGroups(showLoading: Boolean = true) {
        if (isNetworkActiveWithMessage()) {
            viewModel.getGroups(showLoading)
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onSuggestedGroupClicked(group: GroupDto) {
        if (isNetworkActiveWithMessage()) {
            viewModel.joinGroup(group)
        }
    }

    override fun onRemoveSuggestedGroupClicked(group: GroupDto) {
    }

    override fun onYourGroupClicked(group: GroupDto) {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQ_CODE_CREATE_GROUP -> {
                if (resultCode == Activity.RESULT_OK) {
                    getGroups(false)
                }
            }

            AppConstants.REQ_CODE_GROUP_TOPIC -> {
                if (resultCode == Activity.RESULT_OK && data != null && data.hasExtra(AppConstants.EXTRA_INTEREST)) {
                    val topic = data.getParcelableExtra<InterestDto>(AppConstants.EXTRA_INTEREST)
                    TopicGroupsActivity.start(requireActivity(), topic)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog.setLoading(false)
    }
}