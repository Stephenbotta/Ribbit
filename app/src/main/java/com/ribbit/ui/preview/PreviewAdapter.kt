package com.ribbit.ui.preview

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
import com.ribbit.utils.GlideRequests
import kotlinx.android.synthetic.main.fragment_preview.view.*

class PreviewAdapter(private val context: Context, private val glide: GlideRequests,
                     private val images: ArrayList<ImageUrlDto>,
                     private val callback: Callback) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_preview, container, false)

        val image = images[position]
        val path = when (image.mediaType) {
            ApiConstants.POST_TYPE_VIDEO -> {
                image.thumbnail
            }
            else -> {
                image.thumbnail
            }
        }

        when (image.mediaType) {
            ApiConstants.POST_TYPE_IMAGE -> {
                view.btnPlay.gone()
                view.ivImage.visible()
                view.ivVideo.gone()
                glide.load(path)
                        .placeholder(R.color.black)
                        .error(R.color.black)
                        .into(view.ivImage)
            }
            ApiConstants.POST_TYPE_GIF -> {
                view.btnPlay.gone()
                view.ivImage.gone()
                view.ivVideo.visible()
                glide.load(path)
                        .placeholder(R.color.black)
                        .error(R.color.black)
                        .into(view.ivVideo)
            }
            ApiConstants.POST_TYPE_VIDEO -> {
                view.btnPlay.visible()
                view.ivImage.gone()
                view.ivVideo.visible()
                glide.load(path)
                        .placeholder(R.color.black)
                        .error(R.color.black)
                        .into(view.ivVideo)
            }
        }

        view.setOnClickListener {
            if (image.mediaType == ApiConstants.POST_TYPE_VIDEO) {
                callback.playVideo(image)
            }
        }

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any) = view == `object`

    override fun getCount() = images.size

    interface Callback {
        fun playVideo(image: ImageUrlDto)
    }
}