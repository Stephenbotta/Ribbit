package com.checkIt.ui.main.chats

import com.checkIt.data.remote.models.chat.ChatListingDto

interface ChatListCallback {
    fun onClickItem(chat: ChatListingDto)
}