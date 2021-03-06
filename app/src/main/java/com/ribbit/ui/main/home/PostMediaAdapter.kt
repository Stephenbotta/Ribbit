package com.ribbit.ui.main.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.ribbit.R
import com.ribbit.data.remote.ApiConstants
import com.ribbit.data.remote.models.loginsignup.ImageUrlDto
import com.ribbit.extensions.gone
import com.ribbit.extensions.visible
import com.ribbit.utils.GlideApp
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
            ApiConstants.POST_TYPE_VIDEO -> view.ivPlay.visible()
            else -> view.ivPlay.gone()
        }

        view.ivPostImage.setOnClickListener {
            callback.openMediaDetail(media)
        }

        if (media.isMostLiked == true)
            view.ivLiked.visible()
        else
            view.ivLiked.gone()
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
        fun openMediaDetail(media: ImageUrlDto)
    }
}