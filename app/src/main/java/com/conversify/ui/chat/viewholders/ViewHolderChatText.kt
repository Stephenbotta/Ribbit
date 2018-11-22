package com.conversify.ui.chat.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import com.conversify.data.remote.models.chat.ChatMessageDto
import com.conversify.utils.DateTimeUtils
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_chat_text_left.view.*

class ViewHolderChatText(itemView: View,
                         private val glide: GlideRequests) : RecyclerView.ViewHolder(itemView) {
    fun bind(chatMessage: ChatMessageDto) {
        // Display profile image and username if message is of other user
        if (!chatMessage.ownMessage) {
            glide.load(chatMessage.sender?.image?.thumbnail)
                    .into(itemView.ivProfile)
            itemView.tvUserName.text = chatMessage.sender?.userName
        }

        itemView.tvMessage.text = chatMessage.details?.message
        itemView.tvTime.text = DateTimeUtils.getFormattedChatMessageTime(chatMessage.createdDateTimeMillis)
    }
}