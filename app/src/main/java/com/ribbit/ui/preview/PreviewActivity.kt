package com.ribbit.ui.preview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.ribbit.R
import com.ribbit.data.remote.models.loginsignup.ImageUrlDto
import com.ribbit.ui.base.BaseActivity
import com.ribbit.ui.videoplayer.VideoPlayerActivity
import com.ribbit.utils.GlideApp
import kotlinx.android.synthetic.main.activity_preview.*

class PreviewActivity : BaseActivity(), PreviewAdapter.Callback {
    companion object {
        const val EXTRA_IMAGES_LIST = "EXTRA_IMAGES_LIST"
        const val EXTRA_CURRENT_POSITION = "EXTRA_CURRENT_POSITION"

        fun start(context: Context, images: ArrayList<ImageUrlDto>, currentPosition: Int) {
            context.startActivity(Intent(context, PreviewActivity::class.java)
                    .putExtra(EXTRA_IMAGES_LIST, images)
                    .putExtra(EXTRA_CURRENT_POSITION, currentPosition))
        }
    }

    private lateinit var adapter: PreviewAdapter

    private val images by lazy {
        intent.getParcelableArrayListExtra(EXTRA_IMAGES_LIST) ?: ArrayList<ImageUrlDto>()
    }
    private val currentPosition by lazy { intent.getIntExtra(EXTRA_CURRENT_POSITION, 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        adapter = PreviewAdapter(this, GlideApp.with(this), images, this)
        vpImages.adapter = adapter
        vpImages.currentItem = currentPosition

        btnBack.setOnClickListener { finish() }
    }

    override fun playVideo(image: ImageUrlDto) {
        VideoPlayerActivity.start(this, image.videoUrl ?: "")
    }
}