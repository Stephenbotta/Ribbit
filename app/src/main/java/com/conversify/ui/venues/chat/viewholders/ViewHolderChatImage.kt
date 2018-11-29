package com.conversify.ui.venues.chat.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import com.conversify.data.remote.models.chat.ChatMessageDto
import com.conversify.extensions.gone
import com.conversify.extensions.invisible
import com.conversify.extensions.visible
import com.conversify.ui.venues.chat.ResendMessageCallback
import com.conversify.utils.DateTimeUtils
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_chat_image_left.view.*

class ViewHolderChatImage(itemView: View,
                          private val glide: GlideRequests,
                          private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.ivImage.setOnClickListener {
            callback.onImageMessageClicked(chatMessage)
        }
    }

    private lateinit var chatMessage: ChatMessageDto

    fun bind(chatMessage: ChatMessageDto) {
        this.chatMessage = chatMessage

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

        glide.load(chatMessage.details?.image?.original)
                .into(itemView.ivImage)
        itemView.tvTime.text = DateTimeUtils.formatChatMessageTime(chatMessage.createdDateTime)
    }

    interface Callback : ResendMessageCallback {
        fun onImageMessageClicked(chatMessage: ChatMessageDto)
    }
}