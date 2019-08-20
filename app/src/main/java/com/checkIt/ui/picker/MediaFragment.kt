package com.checkIt.ui.picker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import com.checkIt.BuildConfig
import com.checkIt.R
import com.checkIt.ui.custom.LoadingDialog
import com.checkIt.ui.picker.models.MediaSelected
import com.checkIt.ui.picker.models.MediaType
import com.checkIt.utils.AppConstants
import com.checkIt.utils.FileUtils
import com.checkIt.utils.MediaUtils
import com.checkIt.utils.PermissionUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.layout_media_selector.*
import permissions.dispatcher.*
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.*

@RuntimePermissions
class MediaFragment : BottomSheetDialogFragment(), View.OnClickListener {
    companion object {
        const val TAG = "MediaFragment"
        const val ARGUMENT_MAX_COUNT = "ARGUMENT_MAX_COUNT"

        fun newInstance(maxCount: Int): MediaFragment {
            val fragment = MediaFragment()
            val bundle = Bundle()
            bundle.putInt(ARGUMENT_MAX_COUNT, maxCount)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val maxCount by lazy { arguments?.getInt(ARGUMENT_MAX_COUNT) ?: 6 }
    private var file: File? = null
    private var captureType = AppConstants.CAPTURE_TYPE_IMAGE
    private var callback: MediaCallback? = null
    private lateinit var loader: LoadingDialog
    private val cacheDirectory by lazy { FileUtils.getAppCacheDirectoryPath(requireContext()) }

    fun setListeners(callback: MediaCallback) {
        this.callback = callback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_media_selector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnTakePhoto.setOnClickListener(this)
        btnTakeVideo.setOnClickListener(this)
        btnSelectFromGallery.setOnClickListener(this)

        loader = LoadingDialog(view.context)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnTakePhoto -> {
                captureType = AppConstants.CAPTURE_TYPE_IMAGE
                cameraAndStoragePermissionWithPermissionCheck()
            }
            R.id.btnTakeVideo -> {
                captureType = AppConstants.CAPTURE_TYPE_VIDEO
                cameraAndStoragePermissionWithPermissionCheck()
            }
            R.id.btnSelectFromGallery -> {
                captureType = AppConstants.CAPTURE_TYPE_GALLERY
                cameraAndStoragePermissionWithPermissionCheck()
            }
        }
    }

    private fun openCameraToCaptureImage() {
        try {
            val fileName = "IMG_" + UUID.randomUUID().toString()
            val fileSuffix = ".jpg"
            file = createFile(cacheDirectory, fileName, fileSuffix)
            file?.let { imageFile ->
                startActivityForResult(getCameraIntent(imageFile, MediaStore.ACTION_IMAGE_CAPTURE),
                        AppConstants.REQ_CODE_CAMERA_IMAGE)
            }
        } catch (e: Exception) {
            Timber.e("Camera Exception $e")
        }

    }

    private fun openCameraToCaptureVideo() {
        try {
            val fileName = "VID_" + UUID.randomUUID().toString()
            val fileSuffix = ".mp4"
            file = createFile(cacheDirectory, fileName, fileSuffix)
            file?.let { videoFile ->
                startActivityForResult(getCameraIntent(videoFile, MediaStore.ACTION_VIDEO_CAPTURE),
                        AppConstants.REQ_CODE_CAMERA_VIDEO)
            }
        } catch (e: Exception) {
            Timber.e("Video Exception $e")
        }
    }

    /**
     * Returns the camera intent using FileProvider to avoid the FileUriExposedException in Android N and above
     *
     * @param file File for which we need the intent
     */
    private fun getCameraIntent(file: File, action: String): Intent {
        val cameraIntent = Intent(action)

        // Put the uri of the image file as intent extra
        val imageUri = FileProvider.getUriForFile(requireContext(),
                BuildConfig.APPLICATION_ID + ".provider",
                file)

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        // Get a list of all the camera apps
        val resolvedIntentActivities = requireContext().packageManager
                .queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY)

        // Grant uri read/write permissions to the camera apps
        for (resolvedIntentInfo in resolvedIntentActivities) {
            val packageName = resolvedIntentInfo.activityInfo.packageName

            context?.grantUriPermission(packageName, imageUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return cameraIntent
    }

    @Throws(IOException::class)
    private fun createFile(directory: String, fileName: String,
                           fileSuffix: String): File? {
        var file: File? = null
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            val storageDir = File(directory)
            if (!storageDir.mkdirs()) {
                if (!storageDir.exists()) {
                    return null
                }
            }
            file = File.createTempFile(fileName, fileSuffix, storageDir)
        }
        return file
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    fun cameraAndStoragePermission() {
        when (captureType) {
            AppConstants.CAPTURE_TYPE_IMAGE -> {
                openCameraToCaptureImage()
            }
            AppConstants.CAPTURE_TYPE_VIDEO -> {
                openCameraToCaptureVideo()
            }
            AppConstants.CAPTURE_TYPE_GALLERY -> {
                PickerBuilder.with(this)
                        .mode(SelectionMode.BOTH)
                        .setMaxCount(maxCount)
                        .setMaxFileSize(AppConstants.MAXIMUM_VIDEO_SIZE)
                        .pick(AppConstants.REQ_CODE_PICK_MEDIA)
            }
        }
    }

    @OnShowRationale(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    fun showRationaleCameraAndStorage(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(requireContext(), R.string.permission_denied_camera_storage, request)
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    fun neverAskAgainCameraAndStorage() {
        PermissionUtils.showAppSettingsDialog(this, R.string.permission_never_ask_camera_storage, AppConstants.REQ_CODE_APP_SETTINGS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppConstants.REQ_CODE_CAMERA_IMAGE -> {
                    val imageFile = file ?: return
                    val media = MediaSelected(mediaId = UUID.randomUUID().toString(),
                            path = imageFile.absolutePath,
                            type = MediaType.IMAGE)
                    callback?.captureCallback(media)
                    dismiss()
                }

                AppConstants.REQ_CODE_CAMERA_VIDEO -> {
                    val videoFile = file ?: return

                    val thumbnailFile = MediaUtils.getThumbnailFromVideo(videoFile.path
                            ?: "",
                            requireActivity().externalCacheDir?.absolutePath ?: "",
                            MediaStore.Video.Thumbnails.MICRO_KIND)
                    val media = MediaSelected(mediaId = UUID.randomUUID().toString(),
                            path = videoFile.path,
                            type = MediaType.VIDEO,
                            thumbnail = thumbnailFile)
                    callback?.captureCallback(media)
                    dismiss()
                }

                AppConstants.REQ_CODE_PICK_MEDIA -> {
                    if (data != null && data.hasExtra(AppConstants.EXTRA_MULTI_SELECT_IMAGES_LIST) && data.getParcelableArrayListExtra<MediaSelected>(AppConstants.EXTRA_MULTI_SELECT_IMAGES_LIST) != null) {
                        val selectedMedias = data.getParcelableArrayListExtra<MediaSelected>(AppConstants.EXTRA_MULTI_SELECT_IMAGES_LIST)
                        callback?.selectFromGalleryCallback(selectedMedias)
                        dismiss()
                    }
                }
            }
        }
    }

    interface MediaCallback {
        fun captureCallback(file: MediaSelected)
        fun selectFromGalleryCallback(files: List<MediaSelected>)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loader.setLoading(false)
    }
}