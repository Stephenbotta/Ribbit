package com.ribbit.ui.main.chats

import com.ribbit.data.remote.models.chat.ChatListingDto

interface ChatListCallback {
    fun onClickItem(chat: ChatListingDto)
}