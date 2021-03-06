package com.ribbit.ui.conversenearby.post

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ribbit.R
import com.ribbit.data.local.models.AppError
import com.ribbit.data.remote.ApiConstants
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.loginsignup.InterestDto
import com.ribbit.data.remote.models.post.CreatePostRequest
import com.ribbit.databinding.BottomSheetDialogConverseNearbyBinding
import com.ribbit.extensions.handleError
import com.ribbit.extensions.hideKeyboard
import com.ribbit.extensions.longToast
import com.ribbit.extensions.shortToast
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.custom.AppToast
import com.ribbit.ui.custom.LoadingDialog
import com.ribbit.ui.loginsignup.chooseinterests.ChooseInterestsFragment
import com.ribbit.ui.profile.ProfileInterestsAdapter
import com.ribbit.ui.profile.settings.hideinfo.hidestatus.HideStatusActivity
import com.ribbit.utils.*
import com.ribbit.utils.PermissionUtils
import kotlinx.android.synthetic.main.fragment_post_near_by.*
import permissions.dispatcher.*
import java.io.File

@RuntimePermissions
class PostNearByFragment : BaseFragment(), ProfileInterestsAdapter.Callback {

    companion object {
        const val TAG = "PostNearByFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_post_near_by

    private val viewModel by lazy { ViewModelProviders.of(this)[PostNearByViewModel::class.java] }
    private var getSampledImage: GetSampledImage? = null
    private var selectedImage: File? = null
    private lateinit var mediaPicker: MediaPicker
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var postType: String
    private lateinit var interestsAdapter: ProfileInterestsAdapter
    private var flag = 0
    private val selectedUserIdList by lazy { ArrayList<String>() }
    private val interest by lazy { ArrayList<InterestDto>() }
    private var postingIn = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        flag = (activity as PostNearByActivity).getFlag()
        mediaPicker = MediaPicker(this)
        loadingDialog = LoadingDialog(requireContext())
        setListener()
        setupInterestsRecycler()
        observeChanges()
        interest.addAll(viewModel.getProfile().interests ?: emptyList())
        setData(interest)
    }

    private fun setData(list: List<InterestDto>) {
        when (flag) {
            AppConstants.REQ_CODE_CONVERSE_NEARBY -> {
                postType = ApiConstants.CONVERSE_NEARBY
            }
            AppConstants.REQ_CODE_CROSSED_PATH -> {
                postType = ApiConstants.LOOK_NEARBY
            }

        }
        interestsAdapter.displayInterests(list)
    }

    private fun setupInterestsRecycler() {
        interestsAdapter = ProfileInterestsAdapter(this)
        val layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexWrap = FlexWrap.WRAP
        rvConnection.layoutManager = layoutManager
        rvConnection.isNestedScrollingEnabled = false
        rvConnection.adapter = interestsAdapter
    }

    private fun postPic() {
        etPostDescription.hideKeyboard()
        showMediaPickerWithPermissionCheck()
    }

    private fun setListener() {
        btnBack.setOnClickListener { activity?.onBackPressed() }
        btnNext.setOnClickListener {
            val intent = SubmitPostActivity.getStartIntent(requireContext(), flag, requestData(), selectedImage?.absolutePath
                    ?: "")
            startActivityForResult(intent, flag)
        }
        tvPostingIn.setOnClickListener { getPublicly() }
        ivImage.setOnClickListener { postPic() }
        mediaPicker.setImagePickerListener { imageFile ->
            if (imageFile.length() < AppConstants.MAXIMUM_IMAGE_SIZE) {
                getSampledImage?.removeListener()
                getSampledImage?.cancel(true)
                getSampledImage = GetSampledImage()
                getSampledImage?.setListener { sampledImage ->
                    selectedImage = sampledImage
                    GlideApp.with(this)
                            .load(sampledImage)
                            .into(ivImage)
                }
                val imageDirectory = FileUtils.getAppCacheDirectoryPath(requireContext())
                getSampledImage?.sampleImage(imageFile.absolutePath, imageDirectory, 600)
            } else {
                AppToast.longToast(requireContext(), R.string.message_select_smaller_image)
                return@setImagePickerListener
            }
        }

        etPostDescription.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateCreatePostMenuState()
            }
        })
    }

    private fun observeChanges() {
        viewModel.interests.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    val interests = resource.data ?: emptyList()
                    interest.clear()
                    interest.addAll(interests)
                    setData(interest)
                }

                Status.ERROR -> {
                    handleError(resource.error)
                }

                Status.LOADING -> {
                }
            }
        })

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
    }

    private fun isPostValid(): Boolean {
        return !etPostDescription.text.isNullOrBlank()
    }

    private fun updateCreatePostMenuState() {
        btnNext.isEnabled = isPostValid()
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
        mediaPicker.show()
    }

    @OnShowRationale(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun cameraStorageRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(requireContext(), R.string.permission_rationale_camera_storage, request)
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
            AppConstants.REQ_CODE_CONVERSE_NEARBY -> {
                if (resultCode == Activity.RESULT_OK) {
                    activity?.setResult(Activity.RESULT_OK)
                    activity?.finish()
//                    requestData(data.getParcelableExtra(AppConstants.EXTRA_POST_DATA), ApiConstants.CONVERSE_NEARBY)
                }
            }

            AppConstants.REQ_CODE_CROSSED_PATH -> {
                if (resultCode == Activity.RESULT_OK) {
                    activity?.setResult(Activity.RESULT_OK)
                    activity?.finish()
//                    requestData(data.getParcelableExtra(AppConstants.EXTRA_POST_DATA), ApiConstants.LOOK_NEARBY)
                }
            }

            AppConstants.REQ_CODE_CHOOSE_INTERESTS -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val interests = data.getParcelableArrayListExtra<InterestDto>(AppConstants.EXTRA_INTEREST)
                    interest.clear()
                    interest.addAll(interests)
                    setData(interest)
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

            else -> mediaPicker.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun requestData(): CreatePostRequest {
        val createPostRequest = CreatePostRequest()
        createPostRequest.postType = postType
        createPostRequest.postText = etPostDescription.text.toString()
        createPostRequest.selectInterests = interest.mapNotNull { it.id }.toSet().toList()
        if (postingIn) {
            if (selectedUserIdList.size > 0) {
                createPostRequest.postingIn = AppConstants.POST_IN_SELECTED_PEOPLE
            } else {
                createPostRequest.postingIn = AppConstants.POST_IN_FOLLOWERS
            }
        } else {
            createPostRequest.postingIn = AppConstants.POST_IN_PUBLICILY
        }
        createPostRequest.selectedPeople = selectedUserIdList
        return createPostRequest
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

    override fun onDestroy() {
        super.onDestroy()
        getSampledImage?.removeListener()
        getSampledImage?.cancel(true)
        loadingDialog.setLoading(false)
        mediaPicker.clear()
    }
}