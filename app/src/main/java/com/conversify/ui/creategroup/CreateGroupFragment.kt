package com.conversify.ui.creategroup

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.conversify.R
import com.conversify.data.local.models.AppError
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.groups.CreateEditGroupRequest
import com.conversify.data.remote.models.loginsignup.InterestDto
import com.conversify.extensions.handleError
import com.conversify.extensions.hideKeyboard
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.longToast
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.custom.LoadingDialog
import com.conversify.utils.*
import com.conversify.utils.PermissionUtils
import kotlinx.android.synthetic.main.fragment_create_group.*
import permissions.dispatcher.*
import java.io.File

@RuntimePermissions
class CreateGroupFragment : BaseFragment() {
    companion object {
        const val TAG = "CreateGroupFragment"
        private const val ARGUMENT_CATEGORY = "ARGUMENT_CATEGORY"

        fun newInstance(category: InterestDto): Fragment {
            val fragment = CreateGroupFragment()
            val arguments = Bundle()
            arguments.putParcelable(ARGUMENT_CATEGORY, category)
            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var viewModel: CreateGroupViewModel
    private lateinit var request: CreateEditGroupRequest
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var mediaPicker: MediaPicker
    private lateinit var createGroupMenuItem: MenuItem

    private var getSampledImage: GetSampledImage? = null
    private var selectedGroupImageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_create_group

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this)[CreateGroupViewModel::class.java]

        val category = arguments?.getParcelable<InterestDto>(ARGUMENT_CATEGORY)
        request = CreateEditGroupRequest(categoryId = category?.id)

        loadingDialog = LoadingDialog(requireActivity())
        mediaPicker = MediaPicker(this)

        setListeners()
        observeChanges()

        tvCategory.text = category?.name
    }

    private fun setListeners() {
        mediaPicker.setImagePickerListener { imageFile ->
            getSampledImage?.removeListener()
            getSampledImage?.cancel(true)

            getSampledImage = GetSampledImage()
            getSampledImage?.setListener { sampledImage ->
                selectedGroupImageFile = sampledImage
                GlideApp.with(this)
                        .load(sampledImage)
                        .placeholder(R.color.greyImageBackground)
                        .error(R.color.greyImageBackground)
                        .into(ivGroup)
            }
            val imageDirectory = AppUtils.getAppCacheDirectoryPath(requireActivity())
            getSampledImage?.sampleImage(imageFile.absolutePath, imageDirectory, 600)
        }

        ivGroup.setOnClickListener { showImagePickerWithPermissionCheck() }

        etGroupTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateCreateGroupMenuState()
            }
        })
    }

    private fun observeChanges() {
        viewModel.createGroup.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    requireActivity().setResult(Activity.RESULT_OK)
                    requireActivity().finish()
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

    private fun formDataValid(): Boolean {
        return when {
            etGroupTitle?.text.isNullOrBlank() -> false
            else -> true
        }
    }

    private fun updateCreateGroupMenuState() {
        createGroupMenuItem.isEnabled = formDataValid()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_fragment_create_venue, menu)

        val createGroupItem = menu.findItem(R.id.menuCreateVenue)
        createGroupMenuItem = createGroupItem
        updateCreateGroupMenuState()

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuCreateVenue) {
            etGroupTitle.hideKeyboard()
            etGroupTitle.clearFocus()

            if (isNetworkActiveWithMessage()) {
                request.title = etGroupTitle.text?.toString()?.trim()
                request.isPrivate = if (switchPrivateGroup.isChecked) {
                    AppConstants.PRIVATE_TRUE
                } else {
                    AppConstants.PRIVATE_FALSE
                }

                viewModel.createGroup(request, selectedGroupImageFile)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showImagePicker() {
        mediaPicker.show()
    }

    @OnShowRationale(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun cameraStorageRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(requireActivity(), R.string.permission_rationale_camera_storage, request)
    }

    @OnPermissionDenied(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun cameraStorageDenied() {
        activity?.longToast(R.string.permission_denied_camera_storage)
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

    override fun onDestroyView() {
        super.onDestroyView()
        getSampledImage?.removeListener()
        getSampledImage?.cancel(true)
        loadingDialog.setLoading(false)
        mediaPicker.clear()
    }
}