package com.ribbit.ui.venues.details

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.ribbit.R
import com.ribbit.data.local.UserManager
import com.ribbit.data.local.models.AppError
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.chat.MemberDto
import com.ribbit.data.remote.models.groups.AddParticipantsDto
import com.ribbit.data.remote.models.people.UserCrossedDto
import com.ribbit.data.remote.models.venues.VenueDto
import com.ribbit.databinding.BottomSheetDialogInviteVenueBinding
import com.ribbit.extensions.*
import com.ribbit.ui.base.BaseActivity
import com.ribbit.ui.custom.LoadingDialog
import com.ribbit.ui.people.details.PeopleDetailsActivity
import com.ribbit.ui.profile.ProfileActivity
import com.ribbit.ui.venues.addparticipants.AddVenueParticipantsActivity
import com.ribbit.utils.AppConstants
import com.ribbit.utils.GlideApp
import com.ribbit.utils.PermissionUtils
import com.wafflecopter.multicontactpicker.MultiContactPicker
import kotlinx.android.synthetic.main.activity_venue_details.*
import permissions.dispatcher.*
import timber.log.Timber

@RuntimePermissions
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

    //    private val venues by lazy { intent.getParcelableExtra<VenueDto>(EXTRA_VENUE) }
//    private val members by lazy { intent.getParcelableArrayListExtra<MemberDto>(EXTRA_VENUE_MEMBERS) }
    private lateinit var viewModel: VenueDetailsViewModel
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var venues: VenueDto
    private lateinit var venueDetailsAdapter: VenueDetailsAdapter
    private lateinit var members: ArrayList<MemberDto>
    private val gson by lazy { Gson() }
    override fun onSavedInstance(outState: Bundle?, outPersisent: PersistableBundle?) {
        TODO("Not yet implemented")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venue_details)

        viewModel = ViewModelProviders.of(this)[VenueDetailsViewModel::class.java]
        loadingDialog = LoadingDialog(this)
        venues = intent.getParcelableExtra(EXTRA_VENUE)!!
        members = intent.getParcelableArrayListExtra(EXTRA_VENUE_MEMBERS)!!
        observeChanges()
        setupRecyclerView()
        setupVenueDetailsRecycler()
        callApi(venues.id ?: "")
        observeInviteUsersCallback()
        setListener()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_white)
        }

        setVenueTitle(venues.name ?: "")

        val thumbnail = GlideApp.with(this).load(venues.imageUrl?.thumbnail)
        GlideApp.with(this)
                .load(venues.imageUrl?.original)
                .thumbnail(thumbnail)
                .into(ivVenue)

        if (venues.adminId == UserManager.getUserId()) {
            ivEdit.visible()
        } else {
            ivEdit.gone()
        }
    }

    private fun setListener() {
        ivEdit.setOnClickListener {
            ivEdit.gone()
            ivSave.visible()
            etVenueTitle.visible()
            setVenueTitle(" ")
        }
        ivSave.setOnClickListener {
            val title = etVenueTitle.text.toString()
            if (title.isNotBlank()) {
                setVenueTitle(title)
                ivEdit.visible()
                viewModel.editVenueName(title, venues.id ?: "")
                ivSave.gone()
                etVenueTitle.setText("")
                etVenueTitle.gone()
            } else {
                ivEdit.visible()
                ivSave.gone()
                etVenueTitle.gone()
                setVenueTitle(venues.name ?: "")
            }
        }
    }

    private fun setVenueTitle(title: String) {
        val boldTypeface = ResourcesCompat.getFont(this, R.font.roboto_text_bold)
        collapsingToolbar.setExpandedTitleTypeface(boldTypeface)
        collapsingToolbar.setCollapsedTitleTypeface(boldTypeface)
        collapsingToolbar.title = title
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
                        this.venues = venue
                        val member = venue.members ?: arrayListOf()
                        this.members.clear()
                        this.members = member
                        setupVenueDetailsRecycler()
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

        viewModel.editVenueName.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    setVenueTitle(resource.data?.name ?: "")
                }

                Status.ERROR -> {
                    loadingDialog.setLoading(false)
                    if (resource.error != AppError.WaitingForNetwork) {
                        handleError(resource.error)
                    }
                }

                Status.LOADING -> {
                    loadingDialog.setLoading(true)
                }
            }
        })

        viewModel.exitVenue.observe(this, exitOrArchiveVenueObserver)
        viewModel.archiveVenue.observe(this, exitOrArchiveVenueObserver)
    }

    private fun setupRecyclerView() {
        venueDetailsAdapter = VenueDetailsAdapter(GlideApp.with(this), this)
        rvVenueDetails.adapter = venueDetailsAdapter
    }

    private fun setupVenueDetailsRecycler() {

        val items = mutableListOf<Any>()
        items.add(venues)    // Header
        items.add(AddParticipantsDto)    // Add participants
        items.addAll(members)   // Members
        items.add(venues.adminId ?: "")    // Exit group

        venueDetailsAdapter.displayItems(items)
        setupToolbar()
    }

    private fun invitePeopleToVenue() {
        val inflater = layoutInflater
        val binding = DataBindingUtil.inflate<BottomSheetDialogInviteVenueBinding>(inflater, R.layout.bottom_sheet_dialog_invite_venue, null, false)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(binding.root)
        bottomSheetDialog.show()
        binding.tvInvite.setOnClickListener {
            showreadContactWithPermissionCheck()
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
        if (member.user?.id == UserManager.getUserId()) {
            startActivity(Intent(this, ProfileActivity::class.java))
        } else {
            val intent = PeopleDetailsActivity.getStartIntent(this, data,
                    AppConstants.REQ_CODE_BLOCK_USER, member.user?.id ?: "")
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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


    @NeedsPermission(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
    fun showreadContact() {
        MultiContactPicker.Builder(this)
                .setActivityAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                .showPickerForResult(AppConstants.REQ_CODE_SELECT_MULTIPLE_CONTACTS)
    }

    @OnShowRationale(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
    fun showRationaleForContact(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(this, R.string.permission_rationale_contact, request)
    }

    @OnPermissionDenied(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
    fun onContactDenied() {
        longToast(R.string.permission_denied_read_contact)
    }

    @OnNeverAskAgain(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
    fun onreadContactAskAgain() {
        PermissionUtils.showAppSettingsDialog(this, R.string.permission_never_ask_contact, AppConstants.REQ_CODE_APP_SETTINGS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
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