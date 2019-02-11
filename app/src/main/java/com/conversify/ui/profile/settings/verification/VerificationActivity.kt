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
import com.conversify.ui.custom.LoadingDialog
import com.conversify.utils.AppConstants
import com.conversify.utils.GetSampledImage
import com.conversify.utils.MediaPicker
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        mediaPicker = MediaPicker(this)
        loadingDialog = LoadingDialog(this)
        setListener()
        observeChanges()
        setData(viewModel.getProfile())
    }

    private fun setData(profile: ProfileDto) {

        if (profile.isEmailVerified!!) {
            tvEmailVerified.text = getString(R.string.verification_label_verify_email)
            tvEmailVerified.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verify_success, 0)
        }
        tvLabelEmail.text = profile.email
        if (profile.isMobileVerified!!) {
            tvMobileVerified.text = getString(R.string.verification_label_verify_mobile)
            tvMobileVerified.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verify_success, 0)
        }
        tvLabelMobile.text = profile.fullPhoneNumber
    }

    private fun setListener() {
        btnBack.setOnClickListener(this)
        tvEmailVerified.setOnClickListener(this)
        tvMobileVerified.setOnClickListener(this)
        tvUploadDocument.setOnClickListener(this)
    }

    private fun observeChanges() {

        viewModel.verificationApi.observe(this, Observer { resource ->
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

            R.id.tvEmailVerified -> {
            }

            R.id.tvMobileVerified -> {
            }

            R.id.tvUploadDocument -> {
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