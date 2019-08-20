package com.checkIt.ui.search

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.checkIt.R

class SearchPagerAdapter(private val context: Context, manager: FragmentManager) : FragmentPagerAdapter(manager) {
    private val fragments = ArrayList<Fragment>()

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
        return when (position) {
            0 -> context.getString(R.string.search_title_top)
            1 -> context.getString(R.string.search_title_tags)
            2 -> context.getString(R.string.search_title_posts)
            3 -> context.getString(R.string.search_title_groups)
            else -> context.getString(R.string.search_title_venues)
        }
    }
}