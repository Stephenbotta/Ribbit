package com.checkIt.ui.post.newpost

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.checkIt.data.local.models.AppError
import com.checkIt.data.remote.models.Status
import com.checkIt.data.remote.models.groups.GroupDto
import com.checkIt.data.remote.models.groups.GroupPostDto
import com.checkIt.data.remote.models.loginsignup.InterestDto
import com.checkIt.data.remote.models.post.CreatePostRequest
import com.checkIt.databinding.BottomSheetDialogConverseNearbyBinding
import com.checkIt.extensions.*
import com.checkIt.ui.base.BaseFragment
import com.checkIt.ui.custom.LoadingDialog
import com.checkIt.ui.loginsignup.chooseinterests.ChooseInterestsFragment
import com.checkIt.ui.picker.MediaFragment
import com.checkIt.ui.picker.models.MediaSelected
import com.checkIt.ui.picker.models.UploadStatus
import com.checkIt.ui.profile.ProfileInterestsAdapter
import com.checkIt.ui.profile.settings.hideinfo.hidestatus.HideStatusActivity
import com.checkIt.utils.AppConstants
import com.checkIt.utils.AppUtils
import com.checkIt.utils.GlideApp
import com.checkIt.utils.PermissionUtils
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.gms.location.places.ui.PlacePicker
import kotlinx.android.synthetic.main.fragment_new_post.*
import permissions.dispatcher.*

@RuntimePermissions
class NewPostFragment : BaseFragment(), ProfileInterestsAdapter.Callback, MediaFragment.MediaCallback, MediaAdapter.Callback {
    companion object {
        const val TAG = "NewPostFragment"
        private const val ARGUMENT_GROUP = "ARGUMENT_GROUP"
        private const val ARGUMENT_GROUP_POST = "ARGUMENT_GROUP_POST"
        fun newInstance(group: GroupDto? = null): Fragment {
            val fragment = NewPostFragment()
            if (group != null) {
                val arguments = Bundle()
                fragment.arguments = arguments
                arguments.putParcelable(ARGUMENT_GROUP, group)
            }
            return fragment
        }

        fun newInstance(groupPost: GroupPostDto? = null): Fragment {
            val fragment = NewPostFragment()
            if (groupPost != null) {
                val arguments = Bundle()
                fragment.arguments = arguments
                arguments.putParcelable(ARGUMENT_GROUP_POST, groupPost)
            }
            return fragment
        }
    }

    private val group by lazy { arguments?.getParcelable<GroupDto?>(ARGUMENT_GROUP) }
    private val groupPost by lazy { arguments?.getParcelable<GroupPostDto?>(ARGUMENT_GROUP_POST) }
    private lateinit var viewModel: NewPostViewModel
    /*private lateinit var mediaPicker: MediaPicker*/
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var createPostMenuItem: MenuItem
    private val request = CreatePostRequest()
    /*private var getSampledImage: GetSampledImage? = null*/
    private var postingIn = false
    private val selectedUserIdList by lazy { ArrayList<String>() }
    private lateinit var interestsAdapter: ProfileInterestsAdapter
    private val interest by lazy { ArrayList<InterestDto>() }
    private lateinit var adapter: MediaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProviders.of(this)[NewPostViewModel::class.java]
        /*mediaPicker = MediaPicker(this)*/
        loadingDialog = LoadingDialog(requireActivity())
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_new_post

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setListeners()
        observeChanges()
        setupInterestsRecycler()

        setMediaAdapter()
    }

    private fun setMediaAdapter() {
        adapter = MediaAdapter(requireContext(), this)
        rvMedias.adapter = adapter
    }

    private fun setupInterestsRecycler() {
        interestsAdapter = ProfileInterestsAdapter(this)
        val layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexWrap = FlexWrap.WRAP
        rvConnection.layoutManager = layoutManager
        rvConnection.isNestedScrollingEnabled = false
        rvConnection.adapter = interestsAdapter

        interest.addAll(viewModel.getProfile().interests ?: emptyList())
        interestsAdapter.displayInterests(interest)
    }

    private fun setupViews() {
        val group = this.group
        if (group != null) {
            tvLabelPostingInGroup.visible()
            ivGroup.visible()
            tvGroupName.visible()
            GlideApp.with(this)
                    .load(group.imageUrl?.thumbnail)
                    .into(ivGroup)
            tvGroupName.text = group.name

            tvPostingIn.gone()
            tvLabelPostingIn.gone()
            tvLabelInterest.gone()
            rvConnection.gone()
            divider_2.gone()
        } else {
            tvLabelPostingInGroup.gone()
            ivGroup.gone()
            tvGroupName.gone()

            tvPostingIn.visible()
            tvLabelPostingIn.visible()
            tvLabelInterest.visible()
            rvConnection.visible()
            divider_2.visible()
        }
        if (!group?.id.isNullOrEmpty()) {
            request.groupId = group?.id
        }
        if (!groupPost?.id.isNullOrEmpty()) {
            request.postId = groupPost?.id
            request.groupId = groupPost?.group?.id
        }

        /*groupPost?.let { groupPost ->
            etPostText.setText(groupPost.postText)
            if (!groupPost.imageUrl?.thumbnail.isNullOrEmpty()) {
                GlideApp.with(this)
                        .load(groupPost.imageUrl?.thumbnail)
                        .into(ivImage)
                request.imageOriginal = groupPost.imageUrl?.original
                request.imageThumbnail = groupPost.imageUrl?.thumbnail
                ivDelete.visible()
            }
            if (!groupPost.locationName.isNullOrEmpty())
                tvSelectLocation.text = String.format("%s %s", groupPost.locationName, groupPost.locationAddress)
        }*/
    }

    private fun setListeners() {
        ivImage.setOnClickListener {
            it.hideKeyboard()
            showMediaPickerWithPermissionCheck()
        }

        tvSelectLocation.setOnClickListener {
            val builder = PlacePicker.IntentBuilder()
            startActivityForResult(builder.build(activity), AppConstants.REQ_CODE_PLACE_PICKER)
        }

        /*mediaPicker.setImagePickerListener { imageFile ->
            if (imageFile.length() < AppConstants.MAXIMUM_IMAGE_SIZE) {
                getSampledImage?.removeListener()
                getSampledImage?.cancel(true)

                getSampledImage = GetSampledImage()
                getSampledImage?.setListener { sampledImage ->
                    selectedImage = sampledImage
                    GlideApp.with(this)
                            .load(sampledImage)
                            .into(ivImage)
                    createPostMenuItem.isEnabled = true
                    ivDelete.visible()
                }

                val imageDirectory = FileUtils.getAppCacheDirectoryPath(requireActivity())
                getSampledImage?.sampleImage(imageFile.absolutePath, imageDirectory, 600)
            } else {
                AppToast.longToast(requireContext(), R.string.message_select_smaller_image)
                return@setImagePickerListener
            }
        }*/

        /*ivDelete.setOnClickListener {
            ivDelete.gone()
            createPostMenuItem.isEnabled = isValidPost()
            selectedImage = null
            request.imageOriginal = ""
            request.imageThumbnail = ""
            ivImage.setImageDrawable(activity?.getDrawable(R.drawable.ic_add_image))
        }*/

        etPostText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateCreatePostMenuState()
            }
        })

        tvPostingIn.setOnClickListener { getPublicly() }
    }

    private fun isValidPost(): Boolean {
        return if (etPostText.text.toString().isNotBlank())
            return true
        else false
    }

    private fun observeChanges() {
        viewModel.createPost.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    activity?.shortToast(getString(R.string.post_upload_successfully))
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

        viewModel.uploadFileAcknowledgement.observe(this, Observer {
            val media = it?.data ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    adapter.notifyMediaChange(media)
                }
                Status.ERROR -> {
                    adapter.notifyMediaChange(media)
                }
                Status.LOADING -> {
                    adapter.notifyMediaChange(media)
                }
            }
            if (isPostValid() && !adapter.isFilePendingToUpload()) {
                createPost()
            } else if (isPostValid()) {
                createPostMenuItem.isEnabled = true
            }
        })
    }

    private fun isAnyImageUploading(): Boolean {
        return adapter.checkIfAnyFileUploading()
    }

    private fun isPostValid(): Boolean {
        return !etPostText.text.isNullOrBlank() && !isAnyImageUploading()
    }

    private fun updateCreatePostMenuState() {
        if (request.postId.isNullOrEmpty()) {
            createPostMenuItem.isEnabled = isPostValid()
        }
    }

    private fun getPublicly() {
        val inflater = layoutInflater
        val binding = DataBindingUtil.inflate<BottomSheetDialogConverseNearbyBinding>(inflater, R.layout.bottom_sheet_dialog_converse_nearby, null, false)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(binding.root)
        bottomSheetDialog.show()
        binding.tvPublicly.setOnClickListener {
            tvPostingIn.text = getString(R.string.converse_post_label_publicity)
            postingIn = false
            tvPostingIn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_public, 0, 0, 0)
            bottomSheetDialog.dismiss()
        }
        binding.tvFollowers.setOnClickListener {
            val intent = HideStatusActivity.start(requireContext(), AppConstants.REQ_CODE_NEW_POST)
            intent.putExtra(AppConstants.EXTRA_FOLLOWERS, selectedUserIdList)
            startActivityForResult(intent, AppConstants.REQ_CODE_NEW_POST)
            bottomSheetDialog.dismiss()
        }
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showMediaPicker() {
        /*mediaPicker.show()*/
        val maxCount = AppConstants.MAX_FILE_COUNT - adapter.itemCount
        if (maxCount > 0) {
            val mediaFragment = MediaFragment.newInstance(maxCount)
            mediaFragment.setListeners(this)
            mediaFragment.show(fragmentManager, MediaFragment.TAG)
        } else {
            activity?.shortToast(getString(R.string.error_msg_you_can_upload_max_6_files, AppConstants.MAX_FILE_COUNT))
        }
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
        when (requestCode) {
            AppConstants.REQ_CODE_PLACE_PICKER -> {
                if (resultCode == Activity.RESULT_OK) {
                    val place = PlacePicker.getPlace(context, data)
                    request.locationLat = place.latLng.latitude
                    request.locationLong = place.latLng.longitude
                    request.locationName = place.name?.toString()
                    request.locationAddress = place.address?.toString()
                    tvSelectLocation.text = AppUtils.getFormattedAddress(request.locationName, request.locationAddress)
//                    updateCreateVenueMenuState()
                }
            }

            AppConstants.REQ_CODE_NEW_POST -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    postingIn = true
                    tvPostingIn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_group, 0, 0, 0)
                    selectedUserIdList.clear()
                    selectedUserIdList.addAll(data.getStringArrayListExtra(AppConstants.EXTRA_FOLLOWERS))
                    if (selectedUserIdList.isNotEmpty()) {
                        tvPostingIn.text = getString(R.string.hide_info_label_people_count, selectedUserIdList.size)
                    } else {
                        tvPostingIn.text = getString(R.string.converse_path_info_followers)
                    }
                }

            }

            AppConstants.REQ_CODE_CHOOSE_INTERESTS -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val interests = data.getParcelableArrayListExtra<InterestDto>(AppConstants.EXTRA_INTEREST)
                    interest.clear()
                    interest.addAll(interests)
                    interestsAdapter.displayInterests(interests)
                }
            }

            /*else -> mediaPicker.onActivityResult(requestCode, resultCode, data)*/
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_new_post, menu)

        val createPostItem = menu.findItem(R.id.menuPost)
        createPostMenuItem = createPostItem
        updateCreatePostMenuState()

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.menuPost) {
            etPostText.clearFocus()
            etPostText.hideKeyboard()
            createPost()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createPost() {
        if (isNetworkActiveWithMessage()) {
            val medias = adapter.getNewlyAddedMediaFiles()
            if (medias.isNotEmpty()) {
                viewModel.uploadMedias(medias)
                createPostMenuItem.isEnabled = false
            } else {
                val uploadedFiles = adapter.getUploadedMediaFiles()

                //posting publically or to show to particular person
                if (group == null) {
                    request.selectInterests = interest.mapNotNull { it.id }.toSet().toList()
                    if (postingIn) {
                        if (selectedUserIdList.size > 0) {
                            request.postingIn = AppConstants.POST_IN_SELECTED_PEOPLE
                        } else {
                            request.postingIn = AppConstants.POST_IN_FOLLOWERS
                        }
                    } else {
                        request.postingIn = AppConstants.POST_IN_PUBLICILY
                    }
                    request.selectedPeople = selectedUserIdList
                }

                viewModel.createPost(etPostText.text.trim().toString(), uploadedFiles, request)
            }
        }
    }

    override fun onEditInterestsClicked() {
        val fragment = ChooseInterestsFragment.newInstance(true, updateInPref = false, interest = interest, count = 1)
        fragment.setTargetFragment(this, AppConstants.REQ_CODE_CHOOSE_INTERESTS)
        fragmentManager?.apply {
            beginTransaction()
                    .setCustomAnimations(R.anim.slide_up_in, R.anim.slide_up_out,
                            R.anim.slide_down_in, R.anim.slide_down_out)
                    .add(android.R.id.content, fragment, ChooseInterestsFragment.TAG)
                    .addToBackStack(null)
                    .commit()
        }
    }

    override fun captureCallback(file: MediaSelected) {
        adapter.addMediaFile(file)
    }

    override fun selectFromGalleryCallback(files: List<MediaSelected>) {
        adapter.addMediaFiles(files)
    }

    override fun removeMedia(media: MediaSelected) {
        if (media.status != UploadStatus.SENDING) {
            adapter.removeMediaFile(media)
        } else {
            context?.shortToast(R.string.you_can_not_remove_media_while_its_uploading_please_wait)
        }
    }

    override fun resendMedia(media: MediaSelected) {
        if (isNetworkActiveWithMessage()) {
            createPostMenuItem.isEnabled = false
            viewModel.resendMedia(media)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        /*getSampledImage?.removeListener()
        getSampledImage?.cancel(true)*/
        loadingDialog.setLoading(false)
        /*mediaPicker.clear()*/
    }
}