package com.conversify.ui.search

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.conversify.R

/**
 * Created by Manish Bhargav
 */
class SearchPagerAdapter(context: Context, manager: FragmentManager) : FragmentPagerAdapter(manager) {

    val fragments = ArrayList<Fragment>()
    private val context: Context

    init {
        this.context = context
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    // Our custom method that populates this Adapter with Fragments
    fun addFragments(fragment: Fragment) {
        fragments.add(fragment)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val title = if (position == 0) {
            context.getString(R.string.search_title_top)
        } else if (position == 1) {
            context.getString(R.string.search_title_tags)
        } else if (position == 2) {
            context.getString(R.string.search_title_posts)
        } else if (position == 3) {
            context.getString(R.string.search_title_groups)
        } else {
            context.getString(R.string.search_title_venues)
        }
        return title
    }
}