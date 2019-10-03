package com.ribbit.ui.images

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.ribbit.R
import com.ribbit.utils.GlideRequests
import com.ribbit.utils.TouchImageView

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