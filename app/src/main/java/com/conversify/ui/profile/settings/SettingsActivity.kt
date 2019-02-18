package com.conversify.ui.profile.settings

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.support.design.widget.BottomSheetDialog
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.TextView
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.databinding.BottomSheetDialogInvitePeopleBinding
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.longToast
import com.conversify.extensions.startLandingWithClear
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.custom.LoadingDialog
import com.conversify.ui.profile.settings.blockusers.BlockUsersListActivity
import com.conversify.ui.profile.settings.verification.VerificationActivity
import com.conversify.ui.profile.settings.weblink.WebLinkActivity
import com.conversify.utils.AppConstants
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : BaseActivity(), View.OnClickListener {

    private val viewModel by lazy { ViewModelProviders.of(this)[SettingsViewModel::class.java] }
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        loadingDialog = LoadingDialog(this)
        setData(viewModel.getProfile())
        setListener()
        observeChanges()
    }

    private fun setData(profile: ProfileDto) {
        if (profile.isAlertNotifications!!)
            tvAlert.isChecked = profile.isAlertNotifications
        else tvAlert.isChecked = profile.isAlertNotifications
    }

    private fun setListener() {
        btnBack.setOnClickListener(this)
        tvVerification.setOnClickListener(this)
        tvInvitePeople.setOnClickListener(this)
        tvShareContactDetails.setOnClickListener(this)
        tvHidePersonalInfo.setOnClickListener(this)
        tvBlockUsers.setOnClickListener(this)
        tvAccessLocation.setOnClickListener(this)
        tvContactUs.setOnClickListener(this)
        tvTermsAndConditions.setOnClickListener(this)
        tvPush.setOnClickListener(this)
        tvAlert.setOnClickListener(this)
        tvLogout.setOnClickListener(this)
    }

    private fun observeChanges() {

        viewModel.logout.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    startLandingWithClear()
                }

                Status.ERROR -> {
                    loadingDialog.setLoading(false)
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    loadingDialog.setLoading(true)
                }
            }
        })

        viewModel.alert.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    val data = resource.data
                    if (data?.isAlertNotifications!!)
                        tvAlert.isChecked = data.isAlertNotifications
                    else tvAlert.isChecked = data.isAlertNotifications
                }

                Status.ERROR -> {
                    loadingDialog.setLoading(false)
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    loadingDialog.setLoading(true)
                }
            }
        })
    }

    private fun invitePeople() {
        val inflater = layoutInflater
        val binding = DataBindingUtil.inflate<BottomSheetDialogInvitePeopleBinding>(inflater, R.layout.bottom_sheet_dialog_invite_people, null, false)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(binding.root)
        bottomSheetDialog.show()
        binding.tvMail.setOnClickListener {
            sendEmail()
            bottomSheetDialog.dismiss()
        }
        binding.tvMessage.setOnClickListener {
            sendInviteViaMessages()
            bottomSheetDialog.dismiss()
        }
        binding.tvMore.setOnClickListener {
            invitePeopleMoreOptions()
            bottomSheetDialog.dismiss()
        }
        binding.tvCancel.setOnClickListener { bottomSheetDialog.dismiss() }
    }

    private fun showLogoutConfirmationDialog() {
        val dialog = AlertDialog.Builder(this)
                .setMessage(R.string.profile_message_confirm_logout)
                .setPositiveButton(R.string.profile_btn_logout) { _, _ ->
                    if (isNetworkActiveWithMessage()) {
                        viewModel.logout()
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
        dialog.show()
        val typeface = ResourcesCompat.getFont(this, R.font.roboto_text_regular)
        dialog.findViewById<TextView>(android.R.id.message)?.typeface = typeface
    }

    private fun verification() {
        val intent = Intent(this, VerificationActivity::class.java)
        startActivity(intent)
    }

    private fun openPhoneBookList() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, AppConstants.REQ_CODE_CHOOSE_CONTACTS)
    }

    private fun sendEmail() {
        val emailIntent = Intent(android.content.Intent.ACTION_SEND)
        emailIntent.type = "plain/text"
//        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, "manishsharma@code-brew.com")
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Invite")
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.invite_people_option_more_text_message))
        startActivity(emailIntent)
    }

    private fun sendInviteViaMessages() {
        val smsIntent = Intent(Intent.ACTION_VIEW)
        smsIntent.data = Uri.parse("smsto:")
        smsIntent.putExtra("sms_body", getString(R.string.invite_people_option_more_text_message))
        startActivity(smsIntent)
    }

    private fun invitePeopleMoreOptions() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.invite_people_option_more_text_message))
        startActivity(Intent.createChooser(intent, AppConstants.TITLE_SHARE_VIA))
    }

    private fun blockUsers() {
        val intent = Intent(this, BlockUsersListActivity::class.java)
        startActivity(intent)
    }

    private fun shareContactDetails() {
        val profile = viewModel.getProfile()
        val details = "${getString(R.string.edit_profile_label_name)} : ${profile.fullName}\n${getString(R.string.welcome_label_username)} : ${profile.userName}\n" +
                "${getString(R.string.profile_label_bio)} : ${profile.bio}\n${getString(R.string.edit_profile_label_private_info)}\n${getString(R.string.edit_profile_label_email)} : ${profile.email}\n" +
                "${getString(R.string.edit_profile_label_phone)} : ${profile.fullPhoneNumber}\n${getString(R.string.edit_profile_label_gender)} : ${profile.gender}"
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, details)
        startActivity(Intent.createChooser(intent, AppConstants.TITLE_SHARE_VIA))
    }

    private fun accessLocation() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    private fun notificationSettings() {
        val intent = Intent()
        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
//for Android 5-7
        intent.putExtra("app_package", packageName)
        intent.putExtra("app_uid", applicationInfo.uid)
// for Android 8 and above
        intent.putExtra("android.provider.extra.APP_PACKAGE", packageName)
        startActivity(intent)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQ_CODE_CHOOSE_CONTACTS -> {
                if (resultCode == Activity.RESULT_OK) {
                    val contactData = data?.data
                    val phone = contentResolver.query(contactData, null, null, null, null)
                    if (phone!!.moveToFirst()) {
                        val contactNumberName = phone.getString(phone.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        // Todo something when contact number selected
                        longToast(contactNumberName)
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btnBack -> onBackPressed()

            R.id.tvVerification -> verification()

            R.id.tvInvitePeople -> invitePeople()

            R.id.tvShareContactDetails -> shareContactDetails()

            R.id.tvHidePersonalInfo -> {
            }

            R.id.tvBlockUsers -> blockUsers()

            R.id.tvAccessLocation -> accessLocation()

            R.id.tvContactUs -> {}

            R.id.tvTermsAndConditions -> {}

            R.id.tvPush -> notificationSettings()

            R.id.tvAlert -> viewModel.alertNotification(!viewModel.getProfile().isAlertNotifications!!)

            R.id.tvLogout -> showLogoutConfirmationDialog()
        }
    }
}