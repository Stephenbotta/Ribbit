package com.pulse.ui.picker

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import com.pulse.R
import com.pulse.extensions.gone
import com.pulse.extensions.visible
import com.pulse.ui.base.BaseActivity
import com.pulse.ui.picker.albums.AlbumsFragment
import com.pulse.ui.picker.models.MediaSelected
import com.pulse.ui.picker.models.MediaType
import com.pulse.ui.picker.models.PickerMedia
import com.pulse.utils.AppConstants
import com.pulse.utils.FileUtils
import com.pulse.utils.MediaUtils
import kotlinx.android.synthetic.main.activity_picker.*
import timber.log.Timber
import java.io.File

class PickerActivity : BaseActivity(), ToolbarTitleListener {
    companion object {
        const val EXTRA_SELECTION_PARAMS = "EXTRA_SELECTION_PARAMS"

        @JvmStatic
        fun getIntent(context: Context, params: SelectionParams): Intent {
            return Intent(context, PickerActivity::class.java)
                    .putExtra(EXTRA_SELECTION_PARAMS, params)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picker)
        val selectionParams = intent.getParcelableExtra<SelectionParams>(EXTRA_SELECTION_PARAMS)

        tvBack.setOnClickListener { onBackPressed() }
        setToolbarTitle(getString(R.string.image_picker_title_albums))

        val viewModel = ViewModelProviders.of(this)[PickerViewModel::class.java]
        viewModel.start(selectionParams)
        viewModel.getAlbums()

        viewModel.getMediaCountLiveData().observe(this, Observer { count ->
            if (count == null || count == 0) {
                btnSend.text = ""
                btnSend.isEnabled = false
                btnSend.gone()
            } else {
                btnSend.text = getString(R.string.image_picker_btn_send, count)
                if (!btnSend.isEnabled)
                    btnSend.isEnabled = true
                btnSend.visible()
            }
        })

        if (supportFragmentManager.findFragmentByTag(AlbumsFragment.TAG) == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, AlbumsFragment(), AlbumsFragment.TAG)
                    .commit()
        }

        btnSend.setOnClickListener {
            val selectedMedia = viewModel.getSelectedMedias()
            Timber.i("Selected media $selectedMedia")
            val intent = Intent()
            val medias = ArrayList<MediaSelected>()
            selectedMedia.forEach { media ->
                val messageType = when (media) {
                    is PickerMedia.PickerPhoto -> if (FileUtils.isGifFile(File(media.path))) MediaType.GIF
                    else MediaType.IMAGE
                    is PickerMedia.PickerVideo -> {
                        val thumbnailFile = MediaUtils.getThumbnailFromVideo(media.pathVideo,
                                externalCacheDir?.absolutePath ?: "",
                                MediaStore.Video.Thumbnails.MINI_KIND)
                        media.thumbnail = thumbnailFile
                        media.thumbnailFile = thumbnailFile
                        MediaType.VIDEO
                    }
                }
                medias.add(MediaSelected(media.mediaId.toString(), media.path, messageType, media.thumbnail))
            }
            intent.putParcelableArrayListExtra(AppConstants.EXTRA_MULTI_SELECT_IMAGES_LIST, medias)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun setToolbarTitle(title: String) {
        tvTitle.text = title
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (supportFragmentManager.backStackEntryCount == 0) {
            setToolbarTitle(getString(R.string.image_picker_title_albums))
        }
    }
}