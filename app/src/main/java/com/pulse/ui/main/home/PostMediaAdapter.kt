package com.pulse.ui.main.home

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pulse.R
import com.pulse.data.remote.ApiConstants
import com.pulse.data.remote.models.loginsignup.ImageUrlDto
import com.pulse.extensions.gone
import com.pulse.extensions.visible
import com.pulse.utils.GlideApp
import kotlinx.android.synthetic.main.item_post_media.view.*

class PostMediaAdapter(private val context: Context,
                       private val callback: Callback) : PagerAdapter() {
    private val images = ArrayList<ImageUrlDto>()
    private val glide = GlideApp.with(context)

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post_media, container, false)

        val media = images[position]

        glide.load(media.original)
                .into(view.ivPostImage)

        when (media.mediaType) {
            ApiConstants.POST_TYPE_IMAGE -> view.ivPlay.gone()
            ApiConstants.POST_TYPE_VIDEO -> view.ivPlay.visible()
            ApiConstants.POST_TYPE_GIF -> view.ivPlay.gone()
        }

        view.setOnClickListener {
            if (media.mediaType == ApiConstants.POST_TYPE_VIDEO)
                callback.playVideo(media.videoUrl ?: "")
        }
        container.addView(view)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any) = view == `object`

    override fun getCount() = images.size

    fun displayImages(images: ArrayList<ImageUrlDto>) {
        this.images.clear()
        this.images.addAll(images)
        notifyDataSetChanged()
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    interface Callback {
        fun playVideo(videoPath: String)
    }
}