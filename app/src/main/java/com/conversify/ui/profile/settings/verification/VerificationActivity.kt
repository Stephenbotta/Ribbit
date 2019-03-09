package com.conversify.ui.profile.settings.verification

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.data.local.models.AppError
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.extensions.handleError
import com.conversify.extensions.longToast
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.custom.AppToast
import com.conversify.ui.custom.LoadingDialog
import com.conversify.ui.loginsignup.verification.VerificationFragment
import com.conversify.utils.*
import com.conversify.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_verification.*
import permissions.dispatcher.*
import java.io.File

@RuntimePermissions
class VerificationActivity : BaseActivity(), View.OnClickListener {

    private val viewModel by lazy { ViewModelProviders.of(this)[VerificationViewModel::class.java] }
    private var getSampledImage: GetSampledImage? = null
    private var selectedImage: File? = null
    private lateinit var mediaPicker: MediaPicker
    private lateinit var loadingDialog: LoadingDialog
    private var apiFlag = 0
    private var isFragmentAdded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        mediaPicker = MediaPicker(this)
        loadingDialog = LoadingDialog(this)
        setData(viewModel.getProfile())
        setListener()
        observeChanges()
    }

    private fun setData(profile: ProfileDto) {

        if (profile.isEmailVerified == true) {
            tvEmailVerified.text = getString(R.string.verification_label_verify_email)
            tvEmailVerified.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verify_success, 0)
        }
        tvLabelEmail.text = profile.email
        if (profile.isMobileVerified == true) {
            tvMobileVerified.text = getString(R.string.verification_label_verify_mobile)
            tvMobileVerified.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verify_success, 0)
        }
        if (profile.isPassportVerified == true) {
            tvUploadDocument.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verify_success, 0)
        }
        tvLabelMobile.text = profile.fullPhoneNumber
    }

    private fun setListener() {
        btnBack.setOnClickListener(this)
        tvEmailVerified.setOnClickListener(this)
        tvMobileVerified.setOnClickListener(this)
        tvUploadDocument.setOnClickListener(this)
        mediaPicker.setImagePickerListener { imageFile ->
            if (imageFile.length() < AppConstants.MAXIMUM_IMAGE_SIZE) {
                getSampledImage?.removeListener()
                getSampledImage?.cancel(true)

                getSampledImage = GetSampledImage()
                getSampledImage?.setListener { sampledImage ->
                    selectedImage = sampledImage
                    apiFlag = 3
                    viewModel.settingsVerification(hashMapOf(), selectedImage)
                }

                val imageDirectory = FileUtils.getAppCacheDirectoryPath(this)
                getSampledImage?.sampleImage(imageFile.absolutePath, imageDirectory, 600)
            } else {
                AppToast.longToast(applicationContext, R.string.message_select_smaller_image)
                return@setImagePickerListener
            }
        }
    }

    private fun observeChanges() {

        viewModel.verificationApi.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    selectedImage = null
                    when (apiFlag) {
                        1 -> longToast(getString(R.string.verification_api_message_verify_email))
                        2 -> {
                            longToast(getString(R.string.verification_api_message_verify_mobile))
                            isFragmentAdded = true
                            btnBack.text = getText(R.string.back)
                            val fragment = VerificationFragment.newInstance(viewModel.getProfile(), true)
//                            fragment.setTargetFragment(fragment, AppConstants.REQ_CODE_VERIFICATION)
                            supportFragmentManager.beginTransaction()
                                    .replace(R.id.flContainer, fragment, VerificationFragment.TAG)
                                    .addToBackStack(VerificationFragment.TAG)
                                    .commit()
                        }
                        3 -> {
                            longToast(getString(R.string.verification_api_message_verify_passport))
                            tvUploadDocument.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verify_success, 0)
                        }
                    }
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

    private fun verifiedEmail(profile: ProfileDto) {
        if (profile.isEmailVerified == false) {
            apiFlag = 1
            val map = hashMapOf<String, String>()
            map["email"] = profile.email ?: ""
            viewModel.settingsVerification(map, selectedImage)
        }
    }

    private fun verifiedMobile(profile: ProfileDto) {
        if (profile.isMobileVerified == false) {
            apiFlag = 2
            val map = hashMapOf<String, String>()
            map["phoneNumber"] = profile.fullPhoneNumber ?: ""
            viewModel.settingsVerification(map, selectedImage)
        }
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
        when (requestCode) {
            AppConstants.REQ_CODE_VERIFICATION -> {
                if (resultCode == Activity.RESULT_OK) {
                    finish()
                }
            }
            else -> {
                mediaPicker.onActivityResult(requestCode, resultCode, data)
            }
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btnBack -> onBackPressed()

            R.id.tvEmailVerified -> verifiedEmail(viewModel.getProfile())

            R.id.tvMobileVerified -> verifiedMobile(viewModel.getProfile())

            R.id.tvUploadDocument -> showMediaPickerWithPermissionCheck()
        }
    }

    override fun onBackPressed() {
        if (isFragmentAdded) {
            isFragmentAdded = false
            btnBack.text = getText(R.string.verification_label_verification)
            supportFragmentManager.popBackStackImmediate()
        } else super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        getSampledImage?.removeListener()
        getSampledImage?.cancel(true)
        loadingDialog.setLoading(false)
        mediaPicker.clear()
    }
}