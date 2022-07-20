package com.ribbit.ui.post.newpost

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
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.ribbit.R
import com.ribbit.data.local.UserManager
import com.ribbit.data.local.models.AppError
import com.ribbit.data.remote.ApiConstants
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.groups.GroupDto
import com.ribbit.data.remote.models.groups.GroupPostDto
import com.ribbit.data.remote.models.loginsignup.InterestDto
import com.ribbit.data.remote.models.post.CreatePostRequest
import com.ribbit.extensions.*
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.custom.LoadingDialog
import com.ribbit.ui.loginsignup.chooseinterests.ChooseInterestsFragment
import com.ribbit.ui.picker.MediaFragment
import com.ribbit.ui.picker.models.MediaSelected
import com.ribbit.ui.picker.models.MediaType
import com.ribbit.ui.picker.models.UploadStatus
import com.ribbit.ui.pickers.CustomPickerDialog
import com.ribbit.ui.profile.ProfileInterestsAdapter
import com.ribbit.ui.profile.settings.hideinfo.hidestatus.HideStatusActivity
import com.ribbit.utils.AppConstants
import com.ribbit.utils.AppUtils
import com.ribbit.utils.GlideApp
import com.ribbit.utils.PermissionUtils
import kotlinx.android.synthetic.main.fragment_new_post.*
import permissions.dispatcher.*

@RuntimePermissions
class NewPostFragment : BaseFragment(), ProfileInterestsAdapter.Callback, MediaFragment.MediaCallback, MediaAdapter.Callback, CustomPickerDialog.PickerSelectionCallback {
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
    private var createPostMenuItem: MenuItem? = null
    private val request = CreatePostRequest()
    /*private var getSampledImage: GetSampledImage? = null*/
    private var postingIn = false
    private val selectedUserIdList by lazy { ArrayList<String>() }
    /*private lateinit var interestsAdapter: ProfileInterestsAdapter*/
    private val interest by lazy {
        viewModel.getProfile().interests ?: ArrayList()
    }
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

        val userData = UserManager.getProfile()
        tvUserName.text = userData.fullName
        GlideApp.with(this)
                .load(userData.image?.thumbnail)
                .into(ivProfile)

        setListeners()
        observeChanges()
        /*setupInterestsRecycler()*/

        setMediaAdapter()
        setupViews()

//        setPostTypeSpinner()
    }

    private fun setPostTypeSpinner() {
        /* val spinnerArray = ArrayList<String>()
         spinnerArray.add(getString(R.string.dialog_post_label_publicly))
         spinnerArray.add(getString(R.string.dialog_post_label_follower_selected_people))

         val spinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
                 spinnerArray) //selected item will look like a spinner set from XML
         spPostingIn.adapter = spinnerArrayAdapter

         spPostingIn.onItemSelectedListener = object : OnItemSelectedListener {
             override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {
                 when (spinnerArray[position]) {
                     getString(R.string.dialog_post_label_publicly) -> {
                         tvPostingIn.text = getString(R.string.converse_post_label_publicity)
                         postingIn = false
                         tvPostingIn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_public, 0, 0, 0)
                     }
                     getString(R.string.dialog_post_label_follower_selected_people) -> {
                         val intent = HideStatusActivity.start(requireContext(), AppConstants.REQ_CODE_NEW_POST)
                         intent.putExtra(AppConstants.EXTRA_FOLLOWERS, selectedUserIdList)
                         startActivityForResult(intent, AppConstants.REQ_CODE_NEW_POST)
                     }
                 }
             }

             override fun onNothingSelected(parentView: AdapterView<*>?) {}
         }

         spPostingIn.isEnabled = false*/
    }

    private fun setMediaAdapter() {
        adapter = MediaAdapter(requireContext(), this)
        rvMedias.adapter = adapter
    }

    /*private fun setupInterestsRecycler() {
        interestsAdapter = ProfileInterestsAdapter(this)
        val layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexWrap = FlexWrap.WRAP
        rvConnection.layoutManager = layoutManager
        rvConnection.isNestedScrollingEnabled = false
        rvConnection.adapter = interestsAdapter

        interest.addAll(viewModel.getProfile().interests ?: emptyList())
        interestsAdapter.displayInterests(interest)
    }*/

    private fun setupViews() {
        val groupPost = this.groupPost
        if (groupPost == null) {
            val group = this.group
            if (group != null) {
                postingInGroupUi(group)
                request.groupId = group.id
            } else {
                postingOnWallUi()
            }
        } else {
            if (groupPost.group != null) {
                postingInGroupUi(groupPost.group)
            } else {
                postingOnWallUi()
            }
            showPostData(groupPost)
        }
    }

    private fun postingOnWallUi() {
        /*tvLabelPostingInGroup.gone()*/
        /* ivGroup.gone()
         tvGroupName.gone()*/

        tvPostingIn.visible()
        tvLabelPostingIn.visible()
        tvLabelInterest.visible()
        tvInterests.visible()
        /*rvConnection.visible()
        divider_2.visible()*/
    }

    private fun postingInGroupUi(group: GroupDto) {
        /*tvLabelPostingInGroup.visible()*/
        /*ivGroup.visible()
        tvGroupName.visible()
        GlideApp.with(this)
                .load(group.imageUrl?.original)
                .into(ivGroup)
        tvGroupName.text = group.name*/

        tvPostingIn.gone()
        tvLabelPostingIn.gone()
        tvLabelInterest.gone()
        tvInterests.gone()
        /*rvConnection.gone()
        divider_2.gone()*/
    }

    private fun showPostData(groupPost: GroupPostDto) {
        etPostText.setText(groupPost.postText)
        val medias = ArrayList<MediaSelected>()
        groupPost.media.forEach {
            val mediaType = when (it.mediaType) {
                ApiConstants.POST_TYPE_IMAGE -> MediaType.IMAGE
                ApiConstants.POST_TYPE_VIDEO -> MediaType.VIDEO
                else -> MediaType.GIF
            }
            medias.add(MediaSelected(mediaId = it.id ?: "", path = it.original ?: "",
                    original = it.original, type = mediaType, thumbnailPath = it.thumbnail,
                    status = UploadStatus.SENT))
        }

        adapter.addMediaFiles(medias)
        if (!groupPost.locationName.isNullOrEmpty())
            tvLocation.text = String.format("%s %s", groupPost.locationName, groupPost.locationAddress)

        val postingInType = groupPost.postingIn
        if (postingInType == ApiConstants.POSTING_IN_SELECTED_PEOPLE) {
            postingIn = true
            tvPostingIn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_group, 0, R.drawable.ccp_down_arrow, 0)
            selectedUserIdList.clear()
            selectedUserIdList.addAll(groupPost.selectedPeople)
            tvPostingIn.text = getString(R.string.hide_info_label_people_count, selectedUserIdList.size)
        } else if (postingInType == ApiConstants.POSTING_IN_FOLLOWERS) {
            tvPostingIn.text = getString(R.string.converse_path_info_followers)
        }

        interest.clear()
        interest.addAll(groupPost.interests ?: emptyList())

        request.postId = groupPost.id
        request.groupId = groupPost.group?.id
    }

    private fun setListeners() {
        ivImage.setOnClickListener {
            it.hideKeyboard()
            showMediaPickerWithPermissionCheck()
        }

        tvLocation.setOnClickListener {
            /*val builder = PlacePicker.IntentBuilder()
            startActivityForResult(builder.build(activity), AppConstants.REQ_CODE_PLACE_PICKER)*/

            val placeFields = listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME)

            // Start the autocomplete intent.
            val intent = Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, placeFields)
                    .build(requireContext())
            startActivityForResult(intent, AppConstants.REQ_CODE_PLACE_PICKER)
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

        tvPostingIn.setOnClickListener {
            /*spPostingIn.performClick()*/
            getPublicly()
        }

        tvInterests.setOnClickListener {
            fragmentManager?.let { fragmentManager ->
                val dialog = CustomPickerDialog(false, interest.map { it.copy() })
                dialog.setCallback(this)
                dialog.show(fragmentManager, CustomPickerDialog.TAG)
            }
        }
    }

    private fun observeChanges() {
        viewModel.createPost.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    if (groupPost == null)
                        activity?.shortToast(getString(R.string.post_upload_successfully))
                    else
                        activity?.shortToast(R.string.post_updated_successfully)
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
                createPostMenuItem?.isEnabled = true
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
        createPostMenuItem?.isEnabled = isPostValid()

        if (!request.postId.isNullOrEmpty()) {
            createPostMenuItem?.setTitle(R.string.post_btn_edit_post)
        }
    }

    private fun getPublicly() {
        /*val inflater = layoutInflater
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
        }*/

        val popup = PopupMenu(requireContext(), tvPostingIn)
        popup.menu.add(getString(R.string.dialog_post_label_publicly))
        popup.menu.add(getString(R.string.dialog_post_label_follower_selected_people))
        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                getString(R.string.dialog_post_label_publicly) -> {
                    tvPostingIn.text = getString(R.string.converse_post_label_publicity)
                    postingIn = false
                    tvPostingIn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_public, 0, R.drawable.ccp_down_arrow, 0)
                }
                getString(R.string.dialog_post_label_follower_selected_people) -> {
                    val intent = HideStatusActivity.start(requireContext(), AppConstants.REQ_CODE_NEW_POST)
                    intent.putExtra(AppConstants.EXTRA_FOLLOWERS, selectedUserIdList)
                    startActivityForResult(intent, AppConstants.REQ_CODE_NEW_POST)
                }
            }
            true
        }
        popup.show()
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showMediaPicker() {
        /*mediaPicker.show()*/
        val maxCount = AppConstants.MAX_FILE_COUNT - adapter.itemCount
        if (maxCount > 0) {
            val mediaFragment = MediaFragment.newInstance(maxCount)
            mediaFragment.setListeners(this)
            fragmentManager?.let { mediaFragment.show(it, MediaFragment.TAG) }
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
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    request.locationLat = place.latLng?.latitude
                    request.locationLong = place.latLng?.longitude
                    request.locationName = place.name
                    request.locationAddress = place.address
                    tvLocation.text = AppUtils.getFormattedAddress(request.locationName, request.locationAddress)
//                    updateCreateVenueMenuState()
                }
            }

            AppConstants.REQ_CODE_NEW_POST -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    postingIn = true
                    tvPostingIn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_group, 0, R.drawable.ccp_down_arrow, 0)
                    selectedUserIdList.clear()
                    data.getStringArrayListExtra(AppConstants.EXTRA_FOLLOWERS)
                        ?.let { selectedUserIdList.addAll(it) }
                    if (selectedUserIdList.isNotEmpty()) {
                        tvPostingIn.text = getString(R.string.hide_info_label_people_count, selectedUserIdList.size)
                    } else {
                        tvPostingIn.text = getString(R.string.converse_path_info_followers)
                    }
                }
            }

            /*AppConstants.REQ_CODE_CHOOSE_INTERESTS -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val interests = data.getParcelableArrayListExtra<InterestDto>(AppConstants.EXTRA_INTEREST)
                    interest.clear()
                    interest.addAll(interests)
                    interestsAdapter.displayInterests(interests)
                }
            }*/

            /*else -> mediaPicker.onActivityResult(requestCode, resultCode, data)*/
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_new_post, menu)

        val createPostItem = menu.findItem(R.id.menuPost)
        createPostMenuItem = createPostItem
        updateCreatePostMenuState()

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuPost) {
            /*if (adapter.itemCount == 1) {
                context?.shortToast(getString(R.string.error_msg_media_count_should_be_greater_than_1_and_less_than_5))
            } else {*/
            etPostText.clearFocus()
            etPostText.hideKeyboard()
            createPost()
            return true
            /*}*/
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createPost() {
        if (isNetworkActiveWithMessage()) {
            val medias = adapter.getNewlyAddedMediaFiles()
            if (medias.isNotEmpty()) {
                viewModel.uploadMedias(medias)
                createPostMenuItem?.isEnabled = false
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
            createPostMenuItem?.isEnabled = false
            viewModel.resendMedia(media)
        }
    }

    override fun setSelectedItems(selectedDataItems: List<InterestDto>) {
        interest.forEach { it.selected = false }
        selectedDataItems.forEach { item ->
            interest.firstOrNull { it.id == item.id }?.selected = item.selected
        }
        tvInterests?.text = selectedDataItems.joinToString { it.name ?: "" }
    }

    override fun setSelectedItem(item: InterestDto) {}

    override fun onDestroyView() {
        super.onDestroyView()
        /*getSampledImage?.removeListener()
        getSampledImage?.cancel(true)*/
        loadingDialog.setLoading(false)
        /*mediaPicker.clear()*/
    }
}