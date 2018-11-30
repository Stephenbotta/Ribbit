package com.conversify.ui.images

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.conversify.R
import com.conversify.ui.base.BaseActivity
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.activity_images.*

class ImagesActivity : BaseActivity() {
    companion object {
        private const val EXTRA_IMAGES = "EXTRA_IMAGES"
        private const val EXTRA_START_POSITION = "EXTRA_START_POSITION"

        fun start(context: Context, images: ArrayList<String>, startPosition: Int = 0) {
            context.startActivity(Intent(context, ImagesActivity::class.java)
                    .putStringArrayListExtra(EXTRA_IMAGES, images)
                    .putExtra(EXTRA_START_POSITION, startPosition))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images)

        val images = intent.getStringArrayListExtra(EXTRA_IMAGES) ?: emptyList<String>()
        if (images.isEmpty()) return

        vpImages.adapter = ImagesAdapter(images, GlideApp.with(this))

        val startPosition = intent.getIntExtra(EXTRA_START_POSITION, 0)
        vpImages.currentItem = if (startPosition < images.size) startPosition else 0
    }
}