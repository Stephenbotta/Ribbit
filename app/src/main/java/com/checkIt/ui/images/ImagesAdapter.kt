package com.checkIt.ui.images

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.checkIt.R
import com.checkIt.utils.GlideRequests
import com.checkIt.utils.TouchImageView

class ImagesAdapter(private val images: List<String>,
                    private val glide: GlideRequests) : PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = TouchImageView(container.context)

        glide.load(images[position])
                .placeholder(R.color.black)
                .error(R.color.black)
                .into(imageView)

        container.addView(imageView)

        return imageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any) = view == `object`

    override fun getCount() = images.size
}