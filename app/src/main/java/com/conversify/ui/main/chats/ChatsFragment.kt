package com.conversify.ui.main.chats

import com.conversify.R
import com.conversify.ui.base.BaseFragment

class ChatsFragment : BaseFragment() {
    companion object {
        const val TAG = "ChatsFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_chats
}