package com.conversify.ui.chat.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import com.conversify.data.remote.models.chat.ChatMessageDto
import com.conversify.extensions.gone
import com.conversify.extensions.invisible
import com.conversify.extensions.visible
import com.conversify.utils.DateTimeUtils
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_chat_text_left.view.*

abstract class ViewHolderChat(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun displayCommonValues(chatMessage: ChatMessageDto, glide: GlideRequests) {
        // Display profile image and username if message is of other user
        if (!chatMessage.ownMessage) {
            if (chatMessage.showProfileImage) {
                itemView.tvUserName.visible()
                itemView.ivProfile.visible()
            } else {
                itemView.tvUserName.gone()
                itemView.ivProfile.invisible()
            }

            glide.load(chatMessage.sender?.image?.thumbnail)
                    .into(itemView.ivProfile)
            itemView.tvUserName.text = chatMessage.sender?.userName
        }

        if (chatMessage.showDate) {
            itemView.tvDate.visible()
            itemView.tvDate.text = DateTimeUtils.formatChatDateHeader(chatMessage.createdDateTime)
        } else {
            itemView.tvDate.gone()
        }

        itemView.tvTime.text = DateTimeUtils.formatChatMessageTime(chatMessage.createdDateTime)
    }
}