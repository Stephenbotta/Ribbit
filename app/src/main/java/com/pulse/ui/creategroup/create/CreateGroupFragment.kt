package com.pulse.ui.creategroup.create

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.pulse.R
import com.pulse.data.local.models.AppError
import com.pulse.data.remote.models.Status
import com.pulse.data.remote.models.groups.AddParticipantsDto
import com.pulse.data.remote.models.groups.CreateEditGroupRequest
import com.pulse.data.remote.models.groups.CreateGroupHeaderDto
import com.pulse.data.remote.models.loginsignup.InterestDto
import com.pulse.data.remote.models.loginsignup.ProfileDto
import com.pulse.extensions.*
import com.pulse.ui.base.BaseFragment
import com.pulse.ui.creategroup.addparticipants.AddParticipantsActivity
import com.pulse.ui.custom.AppToast
import com.pulse.ui.custom.LoadingDialog
import com.pulse.utils.*
import com.pulse.utils.PermissionUtils
import kotlinx.android.synthetic.main.fragment_create_group.*
import permissions.dispatcher.*

@RuntimePermissions
class CreateGroupFragment : BaseFragment(), CreateGroupAdapter.Callback {
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
    private lateinit var createGroupAdapter: CreateGroupAdapter
    private lateinit var createGroupHeader: CreateGroupHeaderDto

    private var getSampledImage: GetSampledImage? = null

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

        // Create header with initial values
        createGroupHeader = CreateGroupHeaderDto(category?.name, category?.id)

        loadingDialog = LoadingDialog(requireActivity())
        mediaPicker = MediaPicker(this)

        setupCreateGroupRecycler()
        setListeners()
        observeChanges()
    }

    private fun setupCreateGroupRecycler() {
        createGroupAdapter = CreateGroupAdapter(GlideApp.with(this), this)
        rvCreateGroup.adapter = createGroupAdapter

        // Initially add create group header and "Add Participants" item
        val items = listOf(createGroupHeader, AddParticipantsDto)
        createGroupAdapter.displayItems(items)
    }

    private fun setListeners() {
        mediaPicker.setImagePickerListener { imageFile ->
            if (imageFile.length() < AppConstants.MAXIMUM_IMAGE_SIZE) {
                getSampledImage?.removeListener()
                getSampledImage?.cancel(true)

                getSampledImage = GetSampledImage()
                getSampledImage?.setListener { sampledImage ->
                    createGroupHeader.selectedGroupImageFile = sampledImage
                    createGroupAdapter.updateHeader()
                }
                val imageDirectory = FileUtils.getAppCacheDirectoryPath(requireActivity())
                getSampledImage?.sampleImage(imageFile.absolutePath, imageDirectory, 600)
            } else {
                AppToast.longToast(requireContext(), R.string.message_select_smaller_image)
                return@setImagePickerListener
            }
        }
    }

    private fun observeChanges() {
        viewModel.createGroup.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    activity?.shortToast(getString(R.string.create_channel_successfully))
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

    private fun updateCreateGroupMenuState() {
        createGroupMenuItem.isEnabled = !createGroupHeader.groupTitle.isNullOrBlank()
    }

    override fun onGroupImageClicked() {
        showImagePickerWithPermissionCheck()
    }

    override fun onGroupTitleTextChanged() {
        updateCreateGroupMenuState()
    }

    override fun onAddParticipantsClicked() {
        val participantIds = ArrayList(request.participantIds ?: emptyList())
        val intent = AddParticipantsActivity.getStartIntent(requireActivity(), participantIds)
        startActivityForResult(intent, AppConstants.REQ_CODE_ADD_PARTICIPANTS)
    }

    override fun onGroupDescriptionTextChanged() {
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
            rvCreateGroup.hideKeyboard()
            if (isNetworkActiveWithMessage()) {
                request.title = createGroupHeader.groupTitle
                request.isPrivate = if (createGroupHeader.isPrivate) {
                    AppConstants.PRIVATE_TRUE
                } else {
                    AppConstants.PRIVATE_FALSE
                }
                request.description = createGroupHeader.description
                viewModel.createGroup(request, createGroupHeader.selectedGroupImageFile)
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
        if (requestCode == AppConstants.REQ_CODE_ADD_PARTICIPANTS
                && resultCode == Activity.RESULT_OK
                && data != null) {
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
            createGroupHeader.memberCount = participants.size
            createGroupAdapter.displayMembers(participants)
        } else {
            mediaPicker.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        getSampledImage?.removeListener()
        getSampledImage?.cancel(true)
        loadingDialog.setLoading(false)
        mediaPicker.clear()
    }
}