package com.ribbit.ui.images

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import com.ribbit.R
import com.ribbit.ui.base.BaseActivity
import com.ribbit.utils.GlideApp
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

    override fun onSavedInstance(outState: Bundle?, outPersisent: PersistableBundle?) {
        TODO("Not yet implemented")
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