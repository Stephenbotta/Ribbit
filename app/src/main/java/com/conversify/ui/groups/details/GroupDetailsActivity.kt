package com.conversify.ui.groups.details

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import com.conversify.R
import com.conversify.data.local.PrefsManager
import com.conversify.data.local.UserManager
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.chat.MemberDto
import com.conversify.data.remote.models.groups.AddParticipantsDto
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.people.UserCrossedDto
import com.conversify.databinding.BottomSheetDialogInviteVenueBinding
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.sendInviteViaEmail
import com.conversify.extensions.shareText
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.custom.LoadingDialog
import com.conversify.ui.people.details.PeopleDetailsActivity
import com.conversify.ui.profile.ProfileActivity
import com.conversify.ui.venues.addparticipants.AddVenueParticipantsActivity
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.activity_group_details.*

class GroupDetailsActivity : BaseActivity(), GroupDetailsAdapter.Callback {
    companion object {
        private const val EXTRA_FLAG = "EXTRA_FLAG"
        private const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"

        fun getStartIntent(context: Context, groupId: String, flag: Int): Intent {
            return Intent(context, GroupDetailsActivity::class.java)
                    .putExtra(EXTRA_GROUP_ID, groupId)
                    .putExtra(EXTRA_FLAG, flag)
        }
    }

    private lateinit var viewModel: GroupDetailsViewModel
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var group: GroupDto
    private lateinit var adapter: GroupDetailsAdapter
    private var flag = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_details)

        flag = intent.getIntExtra(EXTRA_FLAG, 0)

        viewModel = ViewModelProviders.of(this)[GroupDetailsViewModel::class.java]
        loadingDialog = LoadingDialog(this)

        val groupId = intent.getStringExtra(EXTRA_GROUP_ID)
        callApi(groupId)
        observeChanges()
        setupGroupDetailsRecycler()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_white)
        }

        val boldTypeface = ResourcesCompat.getFont(this, R.font.roboto_text_bold)
        collapsingToolbar.setExpandedTitleTypeface(boldTypeface)
        collapsingToolbar.setCollapsedTitleTypeface(boldTypeface)
        collapsingToolbar.title = group.name

        val thumbnail = GlideApp.with(this).load(group.imageUrl?.thumbnail)
        GlideApp.with(this)
                .load(group.imageUrl?.original)
                .thumbnail(thumbnail)
                .into(ivVenue)
    }

    private fun observeChanges() {

        viewModel.groupDetails.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let { group ->
                        //                        this.venue = venue
                        this.group = group
                        // Display items in correct order
                        val members = group.members ?: listOf()
                        val items = mutableListOf<Any>()
                        items.add(group)   // Header
                        items.add(AddParticipantsDto)    // Add participants
                        items.addAll(members)   // Members
                        items.add(Any())    // Exit group
                        setupToolbar()
                        adapter.displayItems(items)
                    }
                }

                Status.ERROR -> {
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    // Ignored
                }
            }
        })

        viewModel.changeVenueNotifications.observe(this, Observer { resource ->
            resource ?: return@Observer
            when (resource.status) {
                Status.SUCCESS -> {
                    group.notification = resource.data
                    val data = Intent()
                    data.putExtra(AppConstants.EXTRA_GROUP, group)
                    setResult(Activity.RESULT_OK, data)
                }

                Status.ERROR -> {
                    handleError(resource.error)
                }

                Status.LOADING -> {
                }
            }
        })

        val exitOrArchiveVenueObserver = Observer<Resource<Any>> { resource ->
            resource ?: return@Observer
            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    group.isMember = false
//                    val data = Intent()
//                    data.putExtra(AppConstants.EXTRA_GROUP, group)
//                    finishActivity(AppConstants.REQ_CODE_LISTING_GROUP_CHAT)
                    setResult(Activity.RESULT_OK, null)
                    finish()
                }

                Status.ERROR -> {
                    handleError(resource.error)
                    loadingDialog.setLoading(false)
                }

                Status.LOADING -> {
                    loadingDialog.setLoading(true)
                }
            }
        }

        viewModel.exitGroup.observe(this, exitOrArchiveVenueObserver)
        viewModel.archiveVenue.observe(this, exitOrArchiveVenueObserver)
    }

    private fun setupGroupDetailsRecycler() {
        adapter = GroupDetailsAdapter(GlideApp.with(this), this)
        rvVenueDetails.adapter = adapter
    }

    private fun callApi(groupId: String) {
        if (isNetworkActiveWithMessage())
            viewModel.getGroupDetails(groupId)
    }

    private fun invitePeopleToGroup() {
        val inflater = layoutInflater
        val binding = DataBindingUtil.inflate<BottomSheetDialogInviteVenueBinding>(inflater, R.layout.bottom_sheet_dialog_invite_venue, null, false)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(binding.root)
        binding.tvInvite.text = getString(R.string.dialog_group_label_invite)
        bottomSheetDialog.show()
        binding.tvInvite.setOnClickListener {
            sendInviteViaEmail(getString(R.string.group_share_link))
            bottomSheetDialog.dismiss()
        }
        binding.tvCancel.setOnClickListener { bottomSheetDialog.dismiss() }
    }

    override fun onNotificationsChanged(isEnabled: Boolean) {
        viewModel.changeVenueNotifications(group.id ?: "", isEnabled)
    }

    override fun onAddParticipantsClicked() {
        AddVenueParticipantsActivity.start(this, group.id
                ?: "", AppConstants.REQ_CODE_GROUP_DETAILS)
    }

    override fun onMemberClicked(member: MemberDto) {
        val data = UserCrossedDto()
        data.profile = member.user
        PrefsManager.get().save(PrefsManager.PREF_PEOPLE_USER_ID, member.user?.id ?: "")
        if (member.user?.id == UserManager.getUserId()) {
            startActivity(Intent(this, ProfileActivity::class.java))
        } else {
            val intent = PeopleDetailsActivity.getStartIntent(this, data, AppConstants.REQ_CODE_BLOCK_USER)
            startActivity(intent)
        }
    }

    override fun onExitVenueClicked() {
        val message = if (group.adminId == UserManager.getUserId()) {
            getString(R.string.group_details_label_delete_group_question)
        } else {
            getString(R.string.group_details_label_exit_group_question)
        }
        val action = if (group.adminId == UserManager.getUserId()) {
            getString(R.string.group_more_options_label_channel_delete)
        } else {
            getString(R.string.group_more_options_label_exit)
        }
        AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(action) { _, _ ->
                    if (isNetworkActiveWithMessage()) {
                        viewModel.exitGroup(group.id ?: "")
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show()
    }

    override fun onArchiveVenueClicked() {
        AlertDialog.Builder(this)
                .setMessage(R.string.group_details_label_archive_group_question)
                .setPositiveButton(R.string.venue_details_btn_archive) { _, _ ->
                    if (isNetworkActiveWithMessage()) {
                        viewModel.archiveVenue(group.id ?: "")
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_venue_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            R.id.menuShare -> {
                shareText(getString(R.string.group_share_link))
                true
            }

            R.id.menuMore -> {
                invitePeopleToGroup()
                true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingDialog.setLoading(false)
    }
}