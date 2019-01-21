package com.conversify.ui.main

import android.content.Context
import android.support.design.widget.TabLayout
import android.util.AttributeSet
import android.widget.ImageView
import com.conversify.R

class MainBottomTabs : TabLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        val homeTab = newTab()
        homeTab.setIcon(R.drawable.selector_tab_home)
        addTab(homeTab)

        val chatTab = newTab()
        chatTab.setIcon(R.drawable.selector_tab_chats)
        addTab(chatTab)

        val searchUsersTab = newTab()
        val searchUsersView = ImageView(context)
        searchUsersView.setImageResource(R.drawable.selector_tab_search_users)
        searchUsersTab.customView = searchUsersView
        addTab(searchUsersTab)

        val exploreTab = newTab()
        exploreTab.setIcon(R.drawable.selector_tab_explore)
        addTab(exploreTab)

        val profileTab = newTab()
        profileTab.setIcon(R.drawable.selector_tab_notification)
        addTab(profileTab)
    }
}