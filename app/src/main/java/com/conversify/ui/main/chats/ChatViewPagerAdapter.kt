package com.conversify.ui.main.chats

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * Created by Manish Bhargav on 14/1/19
 */
class ChatViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {

    override fun getItem(p0: Int): Fragment {
        var fragment: Fragment? = null
        if (p0 == 0) {
            fragment = IndividualChatFragment()
        } else if (p0 == 1) {
            fragment = GroupChatFragment()
        }
        return fragment!!
    }

    override fun getCount(): Int {
        return 2
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