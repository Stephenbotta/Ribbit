package com.checkIt.ui.chat.viewholders

import android.view.View
import com.checkIt.data.local.UserManager
import com.checkIt.data.remote.models.chat.ChatMessageDto
import com.checkIt.ui.chat.ChatActionCallback
import com.checkIt.utils.DialogsUtil
import com.checkIt.utils.GlideRequests
import kotlinx.android.synthetic.main.item_chat_text_left.view.*

class ViewHolderChatText(itemView: View,
                         private val glide: GlideRequests, private val callback: ActionCallback) : ViewHolderChat(itemView) {

    private lateinit var chatMessage: ChatMessageDto

    init {
        itemView.setOnLongClickListener {
            if (chatMessage.sender?.id == UserManager.getUserId())
                DialogsUtil.openAlertDialog(itemView.context, "text", callback, adapterPosition)
            true
        }
    }

    fun bind(chatMessage: ChatMessageDto) {
        this.chatMessage = chatMessage
        displayCommonValues(chatMessage, glide)
        itemView.tvMessage.text = chatMessage.details?.message
    }

    interface ActionCallback : ChatActionCallback
}