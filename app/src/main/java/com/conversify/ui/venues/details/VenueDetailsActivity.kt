package com.conversify.ui.venues.details

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
import com.conversify.data.remote.models.people.UserCrossedDto
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.databinding.BottomSheetDialogInviteVenueBinding
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.shareText
import com.conversify.extensions.shortToast
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.custom.LoadingDialog
import com.conversify.ui.people.details.PeopleDetailsActivity
import com.conversify.ui.profile.ProfileActivity
import com.conversify.ui.venues.addparticipants.AddVenueParticipantsActivity
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import com.google.gson.Gson
import com.wafflecopter.multicontactpicker.MultiContactPicker
import kotlinx.android.synthetic.main.activity_venue_details.*
import timber.log.Timber


class VenueDetailsActivity : BaseActivity(), VenueDetailsAdapter.Callback {
    companion object {
        private const val EXTRA_VENUE = "EXTRA_VENUE"
        private const val EXTRA_VENUE_MEMBERS = "EXTRA_VENUE_MEMBERS"

        fun getStartIntent(context: Context, venue: VenueDto, members: ArrayList<MemberDto>): Intent {
            return Intent(context, VenueDetailsActivity::class.java)
                    .putExtra(EXTRA_VENUE, venue)
                    .putExtra(EXTRA_VENUE_MEMBERS, members)
        }
    }

    private val venues by lazy { intent.getParcelableExtra<VenueDto>(EXTRA_VENUE) }
    private val members by lazy { intent.getParcelableArrayListExtra<MemberDto>(EXTRA_VENUE_MEMBERS) }
    private lateinit var viewModel: VenueDetailsViewModel
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var venue: VenueDto
    private lateinit var member: MemberDto
    private val gson by lazy { Gson() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venue_details)

        viewModel = ViewModelProviders.of(this)[VenueDetailsViewModel::class.java]
        loadingDialog = LoadingDialog(this)
        setupToolbar()
        observeChanges()
        setupVenueDetailsRecycler()
        callApi(venues.id ?: "")

        observeInviteUsersCallback()
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
        collapsingToolbar.title = venues.name

        val thumbnail = GlideApp.with(this).load(venues.imageUrl?.thumbnail)
        GlideApp.with(this)
                .load(venues.imageUrl?.original)
                .thumbnail(thumbnail)
                .into(ivVenue)
    }

    private fun callApi(venueId: String) {
        if (isNetworkActiveWithMessage())
            viewModel.getVenueDetails(venueId)
    }

    private fun observeChanges() {
        viewModel.venueDetails.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let { venue ->


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
                    venues.notification = resource.data
                    val data = Intent()
                    data.putExtra(AppConstants.EXTRA_VENUE, venues)
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
                    venues.isMember = false
                    val data = Intent()
                    data.putExtra(AppConstants.EXTRA_VENUE, venues)
                    setResult(Activity.RESULT_OK, data)
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

        viewModel.exitVenue.observe(this, exitOrArchiveVenueObserver)
        viewModel.archiveVenue.observe(this, exitOrArchiveVenueObserver)
    }

    private fun setupVenueDetailsRecycler() {
        val venueDetailsAdapter = VenueDetailsAdapter(GlideApp.with(this), this)
        rvVenueDetails.adapter = venueDetailsAdapter

        val items = mutableListOf<Any>()
        items.add(venues)    // Header
        items.add(AddParticipantsDto)    // Add participants
        items.addAll(members)   // Members
        items.add(venues.adminId ?: "")    // Exit group

        venueDetailsAdapter.displayItems(items)
    }

    private fun invitePeopleToVenue() {
        val inflater = layoutInflater
        val binding = DataBindingUtil.inflate<BottomSheetDialogInviteVenueBinding>(inflater, R.layout.bottom_sheet_dialog_invite_venue, null, false)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(binding.root)
        bottomSheetDialog.show()
        binding.tvInvite.setOnClickListener {
            selectContactsForInvite()
//            sendInviteViaEmail(getString(R.string.venue_share_link))
            bottomSheetDialog.dismiss()
        }
        binding.tvCancel.setOnClickListener { bottomSheetDialog.dismiss() }
    }

    override fun onNotificationsChanged(isEnabled: Boolean) {
        viewModel.changeVenueNotifications(venues.id ?: "", isEnabled)
    }

    override fun onAddParticipantsClicked() {
        AddVenueParticipantsActivity.start(this, venues.id
                ?: "", AppConstants.REQ_CODE_VENUE_DETAILS)
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
        val message = if (venues.adminId == UserManager.getUserId()) {
            getString(R.string.venue_details_label_delete_venue_question)
        } else {
            getString(R.string.venue_details_label_exit_venue_question)
        }
        val action = if (venues.adminId == UserManager.getUserId()) {
            getString(R.string.group_more_options_label_channel_delete)
        } else {
            getString(R.string.group_more_options_label_exit)
        }
        AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(action) { _, _ ->
                    if (isNetworkActiveWithMessage()) {
                        viewModel.exitVenue(venues.id ?: "")
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show()
    }

    override fun onArchiveVenueClicked() {
        AlertDialog.Builder(this)
                .setMessage(R.string.venue_details_label_archive_venue_question)
                .setPositiveButton(R.string.venue_details_btn_archive) { _, _ ->
                    if (isNetworkActiveWithMessage()) {
                        viewModel.archiveVenue(venues.id ?: "")
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
                shareText(getString(R.string.venue_share_link))
                true
            }

            R.id.menuMore -> {
                invitePeopleToVenue()
                true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun selectContactsForInvite() {
        MultiContactPicker.Builder(this)
                .setActivityAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                .showPickerForResult(AppConstants.REQ_CODE_SELECT_MULTIPLE_CONTACTS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstants.REQ_CODE_SELECT_MULTIPLE_CONTACTS) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val results = MultiContactPicker.obtainResult(data)
                Timber.d("Selected Contacts ${gson.toJson(results)}")

                val phoneNoArray = ArrayList<String>()
                val emailArray = ArrayList<String>()
                results.forEach {
                    it.phoneNumbers.forEach { pNo -> phoneNoArray.add(pNo.number) }
                    it.emails.forEach { email -> emailArray.add(email) }
                }

                Timber.d("Selected Phone Numbers ${gson.toJson(phoneNoArray)}")
                Timber.d("Selected Emails ${gson.toJson(emailArray)}")

                if (isNetworkActiveWithMessage()) {
                    viewModel.inviteUsersApi(gson.toJson(emailArray), gson.toJson(phoneNoArray),
                            venues.id ?: "")
                }
            }
        }
    }

    private fun observeInviteUsersCallback() {
        viewModel.inviteUsers.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    shortToast(getString(R.string.msg_invite_sent_successfully))
                }
                Status.ERROR -> {
                    loadingDialog.setLoading(false)
                    handleError(it.error)
                }
                Status.LOADING -> {
                    loadingDialog.setLoading(true)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingDialog.setLoading(false)
    }
}