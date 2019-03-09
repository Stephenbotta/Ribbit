package com.conversify.ui.chat.viewholders

import android.view.View
import com.conversify.data.remote.models.chat.ChatMessageDto
import com.conversify.ui.chat.ChatActionCallback
import com.conversify.utils.DialogsUtil
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_chat_text_left.view.*

class ViewHolderChatText(itemView: View,
                         private val glide: GlideRequests, private val callback: ActionCallback) : ViewHolderChat(itemView) {

    init {
        itemView.setOnLongClickListener {
            DialogsUtil.openAlertDialog(itemView.context, "text", callback, adapterPosition)
            true
        }
    }

    fun bind(chatMessage: ChatMessageDto) {
        displayCommonValues(chatMessage, glide)
        itemView.tvMessage.text = chatMessage.details?.message
    }

    interface ActionCallback : ChatActionCallback
}