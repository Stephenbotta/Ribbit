package com.conversify.ui.profile.edit

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import com.conversify.R
import com.conversify.data.local.models.AppError
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.profile.CreateEditProfileRequest
import com.conversify.databinding.BottomSheetDialogGenderBinding
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
        if (!profile.gender.isNullOrEmpty()) {
            when (profile.gender?.toLowerCase()) {
                "male" -> editGender.setText(getString(R.string.dialog_gender_label_male))
                "female" -> editGender.setText(getString(R.string.dialog_gender_label_female))
                "others" -> editGender.setText(getString(R.string.dialog_gender_label_others))
            }
        }
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

        editUserName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                val username = text?.toString() ?: ""
                if (isUsernameValid(username)) {
                    if (username.toLowerCase().equals(viewModel.getProfile().userName?.toLowerCase()))
                        editUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verify_success, 0)
                    else
                        viewModel.checkUsernameAvailability(username)
                } else {
                    viewModel.updateUsernameAvailability(false)
                    editUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verify_failed, 0)
                }
            }
        })

        tvGender.setOnClickListener { getGender() }
    }

    private fun isUsernameValid(username: String): Boolean {
        return when {
            username.isBlank() -> false
            !ValidationUtils.isUsernameLengthValid(username) -> false
            username.contains(" ") -> false
            !ValidationUtils.isUsernameCharactersValid(username) -> false
            else -> true
        }
    }

    private fun checkProfileData() {
        if (!validation())
            return

        if (editWebsite.text.toString().isNotBlank())
            if (!Patterns.WEB_URL.matcher(editWebsite.text.toString()).matches()) {
                editWebsite.error = getString(R.string.error_invalid_website)
                return
            }

        val data = CreateEditProfileRequest()
        data.imageOriginal = viewModel.getProfile().image?.original
        data.imageThumbnail = viewModel.getProfile().image?.thumbnail
        data.fullName = editName.text.toString()
        data.userName = editUserName.text.toString()
        if (editWebsite.text.toString().isBlank())
            editWebsite.setText(" ")
            data.website = editWebsite.text.toString()
        if (editBio.text.toString().isBlank())
            editBio.setText(" ")
        data.bio = editBio.text.toString()
        data.designation = editDesignation.text.toString()
        data.company = editCompany.text.toString()
        if (editGender.text.toString().isNotBlank())
            data.gender = editGender.text.toString().toUpperCase()
        data.email = editEmail.text.toString()
        if (isNetworkActiveWithMessage())
            viewModel.editProfile(data, selectedImage)
    }

    private fun validation(): Boolean {
        val fullName = editName.text.toString()
        val email = editEmail.text.toString()
        val username = editUserName.text.toString()

        return when {
            fullName.isEmpty() -> {
                editName.error = getString(R.string.error_empty_full_name)
                false
            }
            email.isEmpty() -> {
                editEmail.error = getString(R.string.error_empty_email)
                false
            }
            !ValidationUtils.isEmailValid(email) -> {
                editEmail.error = getString(R.string.error_invalid_email)
                false
            }
            username.isBlank() -> {
                editUserName.error = getString(R.string.error_empty_user_name)
                false
            }

            !ValidationUtils.isUsernameLengthValid(username) -> {
                editUserName.error = getString(R.string.error_invalid_user_name_length)
                false
            }

            username.contains(" ") -> {
                editUserName.error = getString(R.string.error_user_name_contains_spaces)
                false
            }

            !ValidationUtils.isUsernameCharactersValid(username) -> {
                editUserName.error = getString(R.string.error_invalid_username_characters)
                false
            }

            else -> true
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

        viewModel.usernameAvailability.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    if (resource.data == true) {
                        editUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verify_success, 0)
                    } else {
                        editUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verify_failed, 0)
                    }
                }

                Status.ERROR -> {
                    editUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verify_failed, 0)
                }

                Status.LOADING -> {

                }
            }
        })
    }

    private fun getGender() {
        val inflater = layoutInflater
        val binding = DataBindingUtil.inflate<BottomSheetDialogGenderBinding>(inflater, R.layout.bottom_sheet_dialog_gender, null, false)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(binding.root)
        bottomSheetDialog.show()
        binding.tvMale.setOnClickListener {
            editGender.setText(binding.tvMale.text.toString())
            bottomSheetDialog.dismiss()
        }
        binding.tvFemale.setOnClickListener {
            editGender.setText(binding.tvFemale.text.toString())
            bottomSheetDialog.dismiss()
        }
        binding.tvOthers.setOnClickListener {
            editGender.setText(binding.tvOthers.text.toString())
            bottomSheetDialog.dismiss()
        }
        binding.tvCancel.setOnClickListener { bottomSheetDialog.dismiss() }
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
            R.id.tvSave -> checkProfileData()
            R.id.tvChangeProfilePhoto -> changeProfilePic()
        }
    }

    private fun changeProfilePic() {
        tvChangeProfilePhoto.hideKeyboard()
        showMediaPickerWithPermissionCheck()
    }

    override fun onDestroy() {
        super.onDestroy()
        getSampledImage?.removeListener()
        getSampledImage?.cancel(true)
        loadingDialog.setLoading(false)
        mediaPicker.clear()
    }
}