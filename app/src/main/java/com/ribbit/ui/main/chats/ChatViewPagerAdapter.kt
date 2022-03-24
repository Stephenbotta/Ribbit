package com.ribbit.ui.main.chats

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ribbit.R

class ChatViewPagerAdapter(private val context: Context, manager: FragmentManager) : FragmentPagerAdapter(manager) {

    val fragments = ArrayList<Fragment>()

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
        return if (position == 0) {
            context.getString(R.string.chat_listing_label_individual)
        } else {
            context.getString(R.string.chat_listing_label_group)
        }
    }
}