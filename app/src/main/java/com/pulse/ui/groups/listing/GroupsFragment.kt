package com.pulse.ui.groups.listing

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.SearchView
import android.view.View
import com.pulse.R
import com.pulse.data.remote.models.Status
import com.pulse.data.remote.models.groups.GroupDto
import com.pulse.data.remote.models.loginsignup.InterestDto
import com.pulse.extensions.handleError
import com.pulse.extensions.isNetworkActiveWithMessage
import com.pulse.extensions.longToast
import com.pulse.ui.base.BaseFragment
import com.pulse.ui.creategroup.create.CreateGroupActivity
import com.pulse.ui.custom.LoadingDialog
import com.pulse.ui.groups.groupposts.GroupPostsActivity
import com.pulse.ui.groups.topicgroups.TopicGroupsActivity
import com.pulse.ui.groups.topics.GroupTopicsFragment
import com.pulse.utils.AppConstants
import com.pulse.utils.GlideApp
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

    private val groupPostsLoadedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.hasExtra(AppConstants.EXTRA_GROUP)) {
                val group = intent.getParcelableExtra<GroupDto>(AppConstants.EXTRA_GROUP)
                if (group != null) {
                    groupsAdapter.resetUnreadCount(group)
                }
            }
        }
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_groups

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)[GroupsViewModel::class.java]
        loadingDialog = LoadingDialog(requireActivity())
        groupsAdapter = GroupsAdapter(GlideApp.with(this), this, viewModel.getOwnProfile())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Register receiver to listen for group posts loaded to reset unread count
        val intentFilter = IntentFilter(AppConstants.ACTION_GROUP_POSTS_LOADED)
        LocalBroadcastManager.getInstance(requireActivity())
                .registerReceiver(groupPostsLoadedReceiver, intentFilter)
        setupGroupsRecycler()
        setupGroupsFab()
        observeChanges()
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
                .setLabel(R.string.groups_label_add_new_channel)
                .setLabelColor(colorPrimary)
                .create())

        fabGroups.addActionItem(SpeedDialActionItem.Builder(R.id.fabTopics, R.drawable.ic_grid)
                .setFabBackgroundColor(colorWhite)
                .setFabImageTintColor(colorPrimary)
                .setLabel(R.string.groups_label_topics)
                .setLabelColor(colorPrimary)
                .create())
//        fabGroups.setOnClickListener { fabOverlay.show() }
        fabGroups.setOnActionSelectedListener { item ->
            return@setOnActionSelectedListener when (item.id) {
                R.id.fabAddGroup -> {
                    val intent = Intent(requireActivity(), CreateGroupActivity::class.java)
                    startActivityForResult(intent, AppConstants.REQ_CODE_CREATE_GROUP)
                    false
                }

                R.id.fabTopics -> {
                    val fragment = GroupTopicsFragment()
                    fragment.setTargetFragment(this, AppConstants.REQ_CODE_GROUP_TOPICS)
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

                    resource.data?.let { group ->
                        if (group.isPrivate == true) {
                            requireActivity().longToast(R.string.venues_message_notification_sent_to_admin)
                            groupsAdapter.updateSuggestedGroup(group)
                        } else {
                            val intent = GroupPostsActivity.start(requireActivity(), group)
                            startActivity(intent)
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

    override fun onStart() {
        super.onStart()
        getGroups()
    }

    override fun onSuggestedGroupClicked(group: GroupDto) {
        if (isNetworkActiveWithMessage()) {
            viewModel.joinGroup(group)
        }
    }

    override fun onYourGroupClicked(group: GroupDto) {
        val intent = GroupPostsActivity.start(requireActivity(), group)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQ_CODE_CREATE_GROUP -> {
                if (resultCode == Activity.RESULT_OK) {
                    getGroups(false)
                }
            }

            AppConstants.REQ_CODE_GROUP_TOPICS -> {
                if (resultCode == Activity.RESULT_OK && data != null && data.hasExtra(AppConstants.EXTRA_INTEREST)) {
                    val topic = data.getParcelableExtra<InterestDto>(AppConstants.EXTRA_INTEREST)
                    val intent = TopicGroupsActivity.getStartIntent(requireActivity(), topic)
                    startActivityForResult(intent, AppConstants.REQ_CODE_TOPIC_GROUPS)
                }
            }

            AppConstants.REQ_CODE_TOPIC_GROUPS -> {
                if (resultCode == Activity.RESULT_OK) {
                    getGroups(false)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog.setLoading(false)
        LocalBroadcastManager.getInstance(requireActivity())
                .unregisterReceiver(groupPostsLoadedReceiver)
    }
}