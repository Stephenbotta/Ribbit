package com.pulse.ui.chat.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import com.pulse.data.remote.models.chat.ChatMessageDto
import com.pulse.extensions.gone
import com.pulse.extensions.invisible
import com.pulse.extensions.visible
import com.pulse.utils.DateTimeUtils
import com.pulse.utils.GlideRequests
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