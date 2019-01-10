package com.conversify.ui.chat.viewholders

import android.view.View
import com.conversify.data.remote.models.chat.ChatMessageDto
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_chat_text_left.view.*

class ViewHolderChatText(itemView: View,
                         private val glide: GlideRequests) : ViewHolderChat(itemView) {
    fun bind(chatMessage: ChatMessageDto) {
        displayCommonValues(chatMessage, glide)
        itemView.tvMessage.text = chatMessage.details?.message
    }
}