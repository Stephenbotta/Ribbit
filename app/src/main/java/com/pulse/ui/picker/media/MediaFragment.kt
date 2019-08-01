package com.pulse.ui.picker.media

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.view.View
import com.pulse.R
import com.pulse.ui.base.BaseFragment
import com.pulse.ui.picker.PickerViewModel
import com.pulse.ui.picker.ToolbarTitleListener
import com.pulse.ui.picker.models.MediaSelected
import com.pulse.ui.picker.models.MediaType
import com.pulse.ui.picker.models.PickerMedia
import com.pulse.utils.AppConstants
import com.pulse.utils.GlideApp
import com.pulse.utils.MediaUtils
import kotlinx.android.synthetic.main.fragment_media.*

class MediaFragment : BaseFragment(), MediaAdapter.Callback {
    companion object {
        const val TAG = "MediaFragment"
        private const val ARGUMENT_BUCKET_ID = "ARGUMENT_BUCKET_ID"

        fun newInstance(bucketId: Int): Fragment {
            val arguments = Bundle()
            arguments.putInt(ARGUMENT_BUCKET_ID, bucketId)
            val fragment = MediaFragment()
            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var viewModel: PickerViewModel

    override fun getFragmentLayoutResId(): Int {
        return R.layout.fragment_media
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(requireActivity())[PickerViewModel::class.java]

        val allowMultipleSelection = viewModel.allowMultipleSelection()
        val mediaAdapter = MediaAdapter(view.context, allowMultipleSelection, GlideApp.with(this), this)
        rvPhotos.setHasFixedSize(true)
        rvPhotos.adapter = mediaAdapter

        val bucketId = arguments?.getInt(ARGUMENT_BUCKET_ID)
        val album = if (bucketId != null) viewModel.getAlbum(bucketId) else null
        if (album != null) {
            val activity = activity
            if (activity is ToolbarTitleListener) {
                activity.setToolbarTitle(album.bucketName)
            }
            if (album.media.isNotEmpty()) {
                viewFlipper.displayedChild = 1
                mediaAdapter.displayMedias(album.media)
            } else {
                viewFlipper.displayedChild = 0
            }
        }
    }

    override fun onMediaClicked(media: PickerMedia) {
        viewModel.mediaClicked(media)
    }

    override fun onMediaSelected(media: PickerMedia) {
        val intent = Intent()
        val messageType = when (media) {
            is PickerMedia.PickerPhoto -> MediaType.IMAGE
            is PickerMedia.PickerVideo -> {
                val thumbnailFile = MediaUtils.getThumbnailFromVideo(media.pathVideo,
                        context?.externalCacheDir?.absolutePath ?: "",
                        MediaStore.Video.Thumbnails.MICRO_KIND)
                media.thumbnail = thumbnailFile
                media.thumbnailFile = thumbnailFile
                MediaType.VIDEO
            }
        }

        val selectedMedia = MediaSelected(media.mediaId.toString(), media.path, messageType, media.thumbnail)

        intent.putExtra(AppConstants.EXTRA_SELECTED_MEDIA, selectedMedia)
        activity?.setResult(Activity.RESULT_OK, intent)
        activity?.finish()
    }

    override fun isValidToAddMedia(): Boolean = viewModel.isValidToAddMedia()
}