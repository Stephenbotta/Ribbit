package com.conversify.ui.profile.edit

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import com.conversify.R
import com.conversify.data.local.models.AppError
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.profile.CreateEditProfileRequest
import com.conversify.extensions.handleError
import com.conversify.extensions.hideKeyboard
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.longToast
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.custom.LoadingDialog
import com.conversify.utils.*
import com.conversify.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_edit_profile.*
import permissions.dispatcher.*
import java.io.File

@RuntimePermissions
class EditProfileActivity : BaseActivity(), View.OnClickListener {

    private val viewModel by lazy { ViewModelProviders.of(this)[EditProfileViewModel::class.java] }
    private var getSampledImage: GetSampledImage? = null
    private var selectedImage: File? = null
    private lateinit var mediaPicker: MediaPicker
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        mediaPicker = MediaPicker(this)
        loadingDialog = LoadingDialog(this)
        setData(viewModel.getProfile())
        setListener()
        observeChanges()
    }

    private fun setData(profile: ProfileDto) {
        GlideApp.with(this)
                .load(profile.image?.original)
                .into(ivProfilePic)
        if (!profile.fullName.isNullOrEmpty())
            editName.setText(profile.fullName)
        if (!profile.userName.isNullOrEmpty())
            editUserName.setText(profile.userName)
        if (!profile.website.isNullOrEmpty())
            editWebsite.setText(profile.website)
        if (!profile.bio.isNullOrEmpty())
            editBio.setText(profile.bio)
        if (!profile.designation.isNullOrEmpty())
            editDesignation.setText(profile.designation)
        if (!profile.company.isNullOrEmpty())
            editCompany.setText(profile.company)
        if (!profile.email.isNullOrEmpty())
            editEmail.setText(profile.email)
        if (!profile.fullPhoneNumber.isNullOrEmpty())
            editPhone.setText(profile.fullPhoneNumber)
        if (!profile.gender.isNullOrEmpty())
            editGender.setText(profile.gender)

    }

    private fun setListener() {
        btnBack.setOnClickListener(this)
        tvSave.setOnClickListener(this)
        tvChangeProfilePhoto.setOnClickListener(this)

        mediaPicker.setImagePickerListener { imageFile ->
            getSampledImage?.removeListener()
            getSampledImage?.cancel(true)

            getSampledImage = GetSampledImage()
            getSampledImage?.setListener { sampledImage ->
                selectedImage = sampledImage
                GlideApp.with(this)
                        .load(sampledImage)
                        .into(ivProfilePic)
            }

            val imageDirectory = FileUtils.getAppCacheDirectoryPath(this)
            getSampledImage?.sampleImage(imageFile.absolutePath, imageDirectory, 600)
        }

    }

    private fun checkProfileData() {
        val data = CreateEditProfileRequest()
        data.imageOriginal = viewModel.getProfile().image?.original
        data.imageThumbnail = viewModel.getProfile().image?.thumbnail
        if (!editName.text.toString().isNullOrEmpty())
            data.fullName = editName.text.toString()
        if (!editUserName.text.toString().isNullOrEmpty())
            data.userName = editUserName.text.toString()
        if (!editWebsite.text.toString().isNullOrEmpty())
            data.website = editWebsite.text.toString()
        if (!editBio.text.toString().isNullOrEmpty())
            data.bio = editBio.text.toString()
        if (!editDesignation.text.toString().isNullOrEmpty())
            data.designation = editDesignation.text.toString()
        if (!editCompany.text.toString().isNullOrEmpty())
            data.company = editCompany.text.toString()
        if (!editGender.text.toString().isNullOrEmpty())
            data.gender = editGender.text.toString()
        if (editEmail.text.toString().isNullOrEmpty() || !Patterns.EMAIL_ADDRESS.matcher(editEmail.text.toString()).matches()) {
            editEmail.error = getString(R.string.error_invalid_email)
        } else {
            data.email = editEmail.text.toString()
            if (isNetworkActiveWithMessage())
                viewModel.editProfile(data, selectedImage)
        }
    }

    private fun observeChanges() {
        viewModel.editProfile.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    setResult(Activity.RESULT_OK)
                    finish()
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
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showMediaPicker() {
        mediaPicker.show()
    }

    @OnShowRationale(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun cameraStorageRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(this, R.string.permission_rationale_camera_storage, request)
    }

    @OnPermissionDenied(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun cameraStorageDenied() {
        longToast(R.string.permission_denied_camera_storage)
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun cameraStorageNeverAsk() {
        PermissionUtils.showAppSettingsDialog(this, R.string.permission_never_ask_camera_storage, AppConstants.REQ_CODE_APP_SETTINGS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mediaPicker.onActivityResult(requestCode, resultCode, data)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btnBack -> onBackPressed()

            R.id.tvSave -> {
                checkProfileData()
            }

            R.id.tvChangeProfilePhoto -> {
                tvChangeProfilePhoto.hideKeyboard()
                showMediaPickerWithPermissionCheck()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        getSampledImage?.removeListener()
        getSampledImage?.cancel(true)
        loadingDialog.setLoading(false)
        mediaPicker.clear()
    }
}