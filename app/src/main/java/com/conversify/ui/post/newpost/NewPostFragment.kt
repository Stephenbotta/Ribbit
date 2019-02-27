package com.conversify.ui.post.newpost

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
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.extensions.*
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.custom.LoadingDialog
import com.conversify.utils.*
import com.conversify.utils.PermissionUtils
import kotlinx.android.synthetic.main.fragment_new_post.*
import permissions.dispatcher.*
import java.io.File

@RuntimePermissions
class NewPostFragment : BaseFragment() {
    companion object {
        const val TAG = "NewPostFragment"
        private const val ARGUMENT_GROUP = "ARGUMENT_GROUP"

        fun newInstance(group: GroupDto? = null): Fragment {
            val fragment = NewPostFragment()
            if (group != null) {
                val arguments = Bundle()
                fragment.arguments = arguments
                arguments.putParcelable(ARGUMENT_GROUP, group)
            }
            return fragment
        }
    }

    private val group by lazy { arguments?.getParcelable<GroupDto?>(ARGUMENT_GROUP) }
    private lateinit var viewModel: NewPostViewModel
    private lateinit var mediaPicker: MediaPicker
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var createPostMenuItem: MenuItem

    private var getSampledImage: GetSampledImage? = null
    private var selectedImage: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProviders.of(this)[NewPostViewModel::class.java]
        mediaPicker = MediaPicker(this)
        loadingDialog = LoadingDialog(requireActivity())
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_new_post

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setListeners()
        observeChanges()
    }

    private fun setupViews() {
        group?.let { group ->
            tvLabelPostingIn.visible()
            ivGroup.visible()
            tvGroupName.visible()

            GlideApp.with(this)
                    .load(group.imageUrl?.thumbnail)
                    .into(ivGroup)
            tvGroupName.text = group.name
        }
    }

    private fun setListeners() {
        ivImage.setOnClickListener {
            it.hideKeyboard()
            showMediaPickerWithPermissionCheck()
        }

        mediaPicker.setImagePickerListener { imageFile ->
            getSampledImage?.removeListener()
            getSampledImage?.cancel(true)

            getSampledImage = GetSampledImage()
            getSampledImage?.setListener { sampledImage ->
                selectedImage = sampledImage
                GlideApp.with(this)
                        .load(sampledImage)
                        .into(ivImage)
                createPostMenuItem.isEnabled = true
            }

            val imageDirectory = FileUtils.getAppCacheDirectoryPath(requireActivity())
            getSampledImage?.sampleImage(imageFile.absolutePath, imageDirectory, 600)
        }

        etPostText.addTextChangedListener(object : TextWatcher {
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
        viewModel.createPost.observe(this, Observer { resource ->
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

    private fun isPostValid(): Boolean {
        return !etPostText.text.isNullOrBlank()
    }

    private fun updateCreatePostMenuState() {
        createPostMenuItem.isEnabled = isPostValid()
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showMediaPicker() {
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
            if (isNetworkActiveWithMessage()) {
                viewModel.createPost(group?.id, etPostText.text.toString(), selectedImage)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        getSampledImage?.removeListener()
        getSampledImage?.cancel(true)
        loadingDialog.setLoading(false)
        mediaPicker.clear()
    }
}