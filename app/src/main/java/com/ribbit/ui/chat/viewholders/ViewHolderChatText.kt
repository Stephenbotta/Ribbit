package com.ribbit.ui.chat.viewholders

import android.view.View
import com.ribbit.data.local.UserManager
import com.ribbit.data.remote.models.chat.ChatMessageDto
import com.ribbit.ui.chat.ChatActionCallback
import com.ribbit.utils.DialogsUtil
import com.ribbit.utils.GlideRequests
import kotlinx.android.synthetic.main.item_chat_text_left.view.*

class ViewHolderChatText(itemView: View, private val glide: GlideRequests,
                         private val callback: ActionCallback) : ViewHolderChat(itemView) {

    private lateinit var chatMessage: ChatMessageDto

    init {
        itemView.setOnLongClickListener {
            if (chatMessage.sender?.id == UserManager.getUserId())
                DialogsUtil.openAlertDialog(itemView.context, chatMessage, callback)
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