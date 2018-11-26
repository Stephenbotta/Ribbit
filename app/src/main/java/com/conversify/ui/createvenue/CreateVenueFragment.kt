package com.conversify.ui.createvenue

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
import com.conversify.data.remote.models.loginsignup.InterestDto
import com.conversify.data.remote.models.venues.CreateEditVenueRequest
import com.conversify.extensions.*
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.custom.LoadingDialog
import com.conversify.utils.*
import com.conversify.utils.PermissionUtils
import com.google.android.gms.location.places.ui.PlacePicker
import kotlinx.android.synthetic.main.fragment_create_venue.*
import permissions.dispatcher.*
import timber.log.Timber
import java.io.File

@RuntimePermissions
class CreateVenueFragment : BaseFragment() {
    companion object {
        const val TAG = "CreateVenueFragment"
        private const val ARGUMENT_CATEGORY = "ARGUMENT_CATEGORY"

        fun newInstance(category: InterestDto): Fragment {
            val fragment = CreateVenueFragment()
            val arguments = Bundle()
            arguments.putParcelable(ARGUMENT_CATEGORY, category)
            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var viewModel: CreateVenueViewModel
    private lateinit var request: CreateEditVenueRequest
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var imagePicker: ImagePicker
    private lateinit var createVenueMenuItem: MenuItem

    private var getSampledImage: GetSampledImage? = null
    private var selectedVenueImageFile: File? = null
    private var selectedVerificationFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_create_venue

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this)[CreateVenueViewModel::class.java]

        val category = arguments?.getParcelable<InterestDto>(ARGUMENT_CATEGORY)
        request = CreateEditVenueRequest(categoryId = category?.id)

        loadingDialog = LoadingDialog(requireActivity())
        imagePicker = ImagePicker(this)

        setListeners()
        observeChanges()

        tvCategory.text = category?.name
    }

    private fun setListeners() {
        imagePicker.setImagePickerListener { imageFile ->
            getSampledImage?.removeListener()
            getSampledImage?.cancel(true)

            getSampledImage = GetSampledImage()
            getSampledImage?.setListener { sampledImage ->
                selectedVenueImageFile = sampledImage
                GlideApp.with(this)
                        .load(sampledImage)
                        .placeholder(R.color.greyImageBackground)
                        .error(R.color.greyImageBackground)
                        .into(ivVenue)
            }
            val imageDirectory = requireActivity().externalCacheDir?.absolutePath ?: ""
            getSampledImage?.sampleImage(imageFile.absolutePath, imageDirectory, 600)
        }

        ivVenue.setOnClickListener { showImagePickerWithPermissionCheck() }

        etVenueLocation.setOnClickListener {
            val builder = PlacePicker.IntentBuilder()
            startActivityForResult(builder.build(activity), AppConstants.REQ_CODE_PLACE_PICKER)
        }

        etDateTime.setOnClickListener {
            DateTimePicker(requireActivity(),
                    minDateMillis = System.currentTimeMillis()) { selectedDateTime ->
                etDateTime.setText(DateTimeUtils.formatVenueDateTime(selectedDateTime))
                request.dateTimeMillis = selectedDateTime.toInstant().toEpochMilli()
                updateCreateVenueMenuState()
            }.show()
        }

        etVenueTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateCreateVenueMenuState()
            }
        })

        etVenueTags.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateCreateVenueMenuState()
            }
        })

        clUploadDocument.setOnClickListener {
            FileUtils.showFilePicker(this, AppConstants.REQ_CODE_FILE_PICKER,
                    arrayOf(FileUtils.MIME_TYPE_JPG,
                            FileUtils.MIME_TYPE_PNG,
                            FileUtils.MIME_TYPE_DOC,
                            FileUtils.MIME_TYPE_PDF,
                            FileUtils.MIME_TYPE_TEXT))
        }
    }

    private fun observeChanges() {
        viewModel.createVenue.observe(this, Observer { resource ->
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
            etVenueTitle?.text.isNullOrBlank() -> false
            etVenueLocation?.text.isNullOrBlank() -> false
            etVenueTags?.text.isNullOrBlank() -> false
            etDateTime?.text.isNullOrBlank() -> false
            else -> true
        }
    }

    private fun updateCreateVenueMenuState() {
        createVenueMenuItem.isEnabled = formDataValid()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_fragment_create_venue, menu)

        val createVenueItem = menu.findItem(R.id.menuCreateVenue)
        createVenueMenuItem = createVenueItem
        updateCreateVenueMenuState()

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuCreateVenue) {
            etVenueTitle.hideKeyboard()
            etVenueTitle.clearFocus()
            etVenueTags.clearFocus()
            etVenueOwnerName.clearFocus()

            val tagsString = etVenueTags.text?.toString()?.trim() ?: ""
            val tags = AppUtils.getTagsFromString(tagsString)

            if (tags.isEmpty()) {
                requireActivity().shortToast(R.string.create_venue_message_please_add_at_least_one_tag)
                return false
            }

            if (isNetworkActiveWithMessage()) {
                request.title = etVenueTitle.text?.toString()?.trim()
                request.tags = tags
                request.isPrivate = if (switchPrivateVenue.isChecked) {
                    AppConstants.PRIVATE_TRUE
                } else {
                    AppConstants.PRIVATE_FALSE
                }

                val ownerName = etVenueOwnerName.text?.toString()?.trim()
                if (!ownerName.isNullOrBlank()) {
                    request.ownerName = ownerName
                }

                viewModel.createVenue(request, selectedVenueImageFile, selectedVerificationFile)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showImagePicker() {
        imagePicker.showImagePicker()
    }

    @OnShowRationale(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun cameraStorageRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(requireActivity(), R.string.permission_rationale_camera_storage, request)
    }

    @OnPermissionDenied(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun cameraStorageDenied() {
        activity?.shortToast(R.string.permission_denied_camera_storage)
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
            AppConstants.REQ_CODE_PLACE_PICKER -> {
                if (resultCode == Activity.RESULT_OK) {
                    val place = PlacePicker.getPlace(context, data)
                    request.latitude = place.latLng.latitude
                    request.longitude = place.latLng.longitude
                    request.locationName = place.name?.toString()
                    request.locationAddress = place.address?.toString()
                    etVenueLocation.setText(AppUtils.getFormattedAddress(request.locationName, request.locationAddress))
                    updateCreateVenueMenuState()
                }
            }

            AppConstants.REQ_CODE_FILE_PICKER -> {
                if (resultCode == Activity.RESULT_OK) {
                    val verificationFile = FileUtils.getSelectedFileFromResult(requireActivity(), data)
                    if (verificationFile != null) {
                        if (verificationFile.length() <= AppConstants.VENUE_VERIFICATION_FILE_SIZE_LIMIT) {
                            Timber.i(verificationFile.absolutePath)
                            selectedVerificationFile = verificationFile
                            ivVerificationSelected.visible()
                        } else {
                            requireActivity().longToast(R.string.create_venue_message_verification_file_size_should_be)
                        }
                    }
                }
            }

            else -> {
                imagePicker.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        getSampledImage?.removeListener()
        getSampledImage?.cancel(true)
        loadingDialog.setLoading(false)
        imagePicker.clear()
    }
}