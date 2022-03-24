package com.ribbit.ui.main

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.ribbit.R

class MainBottomTabs : TabLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        val homeTab = newTab()
        homeTab.setIcon(R.drawable.selector_tab_home)
        addTab(homeTab)

        val chatTab = newTab()
        chatTab.setIcon(R.drawable.selector_tab_chats)
        addTab(chatTab)

        /*val searchUsersTab = newTab()
        val searchUsersView = ImageView(context)
        searchUsersView.setImageResource(R.drawable.selector_tab_search_users)
        searchUsersTab.customView = searchUsersView
        addTab(searchUsersTab)*/

        val exploreTab = newTab()
        exploreTab.setIcon(R.drawable.selector_tab_twitter)
        addTab(exploreTab)

        val notificationTab = newTab()
        notificationTab.customView = getTabCustomView()
        addTab(notificationTab)

        val profileTab = newTab()
        profileTab.setIcon(R.drawable.selector_tab_profile)
        addTab(profileTab)
    }

    private fun getTabCustomView(): View? {
        val customView = LayoutInflater.from(context).inflate(R.layout.item_custom_bottom_tab_icon, null, false)
        val ivNotification = customView.findViewById(R.id.ivNotification) as ImageView
        ivNotification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.selector_tab_rewards))
        return customView
    }
}