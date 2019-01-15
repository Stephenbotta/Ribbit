package com.conversify.ui.main.chats

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.conversify.ui.main.chats.group.GroupChatFragment
import com.conversify.ui.main.chats.individual.IndividualChatFragment

/**
 * Created by Manish Bhargav on 14/1/19
 */
class ChatViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {

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
        val title = if (position == 0) {
            "Individual"//getString(R.string.chat_listing_label_individual)
        } else {
            "Group"//getString(R.string.chat_listing_label_group)
        }
        return title
    }
}