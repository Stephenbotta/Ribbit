package com.conversify.ui.images

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.conversify.R
import com.conversify.utils.GlideRequests

class ImagesAdapter(private val images: List<String>,
                    private val glide: GlideRequests) : PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(container.context)

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