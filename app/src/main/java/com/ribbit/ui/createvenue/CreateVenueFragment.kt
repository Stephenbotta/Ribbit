package com.ribbit.ui.createvenue

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.ribbit.R
import com.ribbit.data.local.models.AppError
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.loginsignup.InterestDto
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.data.remote.models.venues.CreateEditVenueRequest
import com.ribbit.extensions.*
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.creategroup.addparticipants.AddParticipantsActivity
import com.ribbit.ui.creategroup.create.CreateGroupAdapter
import com.ribbit.ui.custom.AppToast
import com.ribbit.ui.custom.LoadingDialog
import com.ribbit.utils.*
import com.ribbit.utils.PermissionUtils
import kotlinx.android.synthetic.main.fragment_create_venue.*
import permissions.dispatcher.*
import timber.log.Timber
import java.io.File

@RuntimePermissions
class CreateVenueFragment : BaseFragment(), CreateGroupAdapter.Callback {
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
    private lateinit var mediaPicker: MediaPicker
    private lateinit var createVenueMenuItem: MenuItem
    private var memberCount = 0
    private lateinit var createGroupAdapter: CreateGroupAdapter

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
        mediaPicker = MediaPicker(this)

        setListeners()
        observeChanges()
        tvCategory.text = category?.name
        tvLabelMembers.text = context?.getString(R.string.venue_details_label_members_with_count, memberCount)
        setupCreateGroupRecycler()
    }

    private fun setupCreateGroupRecycler() {
        createGroupAdapter = CreateGroupAdapter(GlideApp.with(this), this)
        rvParticipants.adapter = createGroupAdapter
    }

    private fun setListeners() {
        mediaPicker.setImagePickerListener { imageFile ->
            if (imageFile.length() < AppConstants.MAXIMUM_IMAGE_SIZE) {
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
                val imageDirectory = FileUtils.getAppCacheDirectoryPath(requireActivity())
                getSampledImage?.sampleImage(imageFile.absolutePath, imageDirectory, 600)
            } else {
                AppToast.longToast(requireContext(), getString(R.string.message_select_smaller_image))
                return@setImagePickerListener
            }
        }

        ivVenue.setOnClickListener { showImagePickerWithPermissionCheck() }

        etVenueLocation.setOnClickListener {
            /*val builder = PlacePicker.IntentBuilder()
            startActivityForResult(builder.build(activity), AppConstants.REQ_CODE_PLACE_PICKER)*/

            val placeFields = listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME)

            // Start the autocomplete intent.
            val intent = Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, placeFields)
                    .build(requireContext())
            startActivityForResult(intent, AppConstants.REQ_CODE_PLACE_PICKER)
        }

        etDateTime.setOnClickListener {
            DateTimePicker(requireActivity(),
                    minDateMillis = System.currentTimeMillis()) { selectedDateTime ->
                etDateTime.setText(DateTimeUtils.formatVenueDetailsDateTime(selectedDateTime))
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
            showFilePickerWithPermissionCheck()
        }

        addParticipants.setOnClickListener {
            val participantIds = ArrayList(request.participantIds ?: emptyList())
            val intent = AddParticipantsActivity.getStartIntent(requireActivity(), participantIds)
            startActivityForResult(intent, AppConstants.REQ_CODE_ADD_PARTICIPANTS)
        }
    }

    private fun observeChanges() {
        viewModel.createVenue.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    activity?.shortToast(getString(R.string.venue_created_successfully))
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
            request.dateTimeMillis ?: 0 < System.currentTimeMillis() -> false
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
            val tags = AppUtils.getHashTagsFromString(tagsString, false)

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

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun showFilePicker() {
        FileUtils.showFilePicker(this, AppConstants.REQ_CODE_FILE_PICKER,
                arrayOf(FileUtils.MIME_TYPE_JPG,
                        FileUtils.MIME_TYPE_PNG,
                        FileUtils.MIME_TYPE_DOC,
                        FileUtils.MIME_TYPE_PDF,
                        FileUtils.MIME_TYPE_TEXT))
    }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun readStorageRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(requireActivity(), R.string.permission_rationale_storage, request)
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun readStorageDenied() {
        activity?.longToast(R.string.permission_denied_storage)
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun readStorageNeverAsk() {
        PermissionUtils.showAppSettingsDialog(this, R.string.permission_never_ask_storage, AppConstants.REQ_CODE_APP_SETTINGS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQ_CODE_PLACE_PICKER -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    request.latitude = place.latLng?.latitude
                    request.longitude = place.latLng?.longitude
                    request.locationName = place.name
                    request.locationAddress = place.address
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

            AppConstants.REQ_CODE_ADD_PARTICIPANTS -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val participants = data.getParcelableArrayListExtra<ProfileDto>(AppConstants.EXTRA_PARTICIPANTS)

                    // Update member ids in the create group request
                    if (participants.isEmpty()) {
                        request.participantIds = null
                    } else {
                        request.participantIds = participants.asSequence()
                                .onEach { it.isSelected = false }   // Set selected to false for all
                                .mapNotNull { it.id }   // Map to non-null ids
                                .toList()
                    }
                    // Display the selected participants and update the member count
                    memberCount = request.participantIds?.size ?: 0
                    tvLabelMembers.text = context?.getString(R.string.venue_details_label_members_with_count, memberCount)
                    createGroupAdapter.displayMembers(participants)
                }
            }

            else -> {
                mediaPicker.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onGroupImageClicked() {
    }

    override fun onGroupTitleTextChanged() {
    }

    override fun onGroupDescriptionTextChanged() {
    }

    override fun onAddParticipantsClicked() {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        getSampledImage?.removeListener()
        getSampledImage?.cancel(true)
        loadingDialog.setLoading(false)
        mediaPicker.clear()
    }
}