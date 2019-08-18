package com.checkIt.ui.chat.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import com.checkIt.data.local.UserManager
import com.checkIt.data.remote.models.chat.ChatMessageDto
import com.checkIt.data.remote.models.chat.MessageStatus
import com.checkIt.extensions.gone
import com.checkIt.extensions.isNetworkActiveWithMessage
import com.checkIt.extensions.visible
import com.checkIt.ui.chat.ChatActionCallback
import com.checkIt.ui.chat.ResendMessageCallback
import com.checkIt.utils.DialogsUtil
import com.checkIt.utils.GlideRequests
import kotlinx.android.synthetic.main.item_chat_video_left.view.*

class ViewHolderChatVideo(itemView: View,
                          private val glide: GlideRequests,
                          private val callback: Callback, private val actionCallback: ActionCallback) : ViewHolderChat(itemView) {
    init {
        itemView.btnResend.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION && itemView.context.isNetworkActiveWithMessage()) {
                chatMessage.messageStatus = MessageStatus.SENDING
                updateMessageStatus(chatMessage.messageStatus)
                callback.onResendMessageClicked(chatMessage)
            }
        }

        itemView.ivThumbnail.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                callback.onVideoMessageClicked(chatMessage)
            }
        }

        itemView.ivThumbnail.setOnLongClickListener {
            if (chatMessage.sender?.id == UserManager.getUserId())
                DialogsUtil.openAlertDialog(itemView.context, "video", actionCallback, adapterPosition)
            true
        }
    }

    private lateinit var chatMessage: ChatMessageDto

    fun bind(chatMessage: ChatMessageDto) {
        this.chatMessage = chatMessage
        displayCommonValues(chatMessage, glide)

        // Display local file if it exists otherwise display the image from the url
        val image: Any? = if (chatMessage.localFileThumbnail != null) {
            chatMessage.localFileThumbnail
        } else {
            chatMessage.details?.image?.original
        }
        glide.load(image)
                .into(itemView.ivThumbnail)

        updateMessageStatus(chatMessage.messageStatus)
    }

    private fun updateMessageStatus(status: MessageStatus) {
        when (status) {
            MessageStatus.SENDING -> {
                itemView.progressBar.visible()
                itemView.btnResend.isEnabled = false
                itemView.btnResend.gone()
                itemView.ivPlay.gone()
            }

            MessageStatus.SENT -> {
                itemView.progressBar.gone()
                itemView.btnResend.isEnabled = false
                itemView.btnResend.gone()
                itemView.ivPlay.visible()
            }

            MessageStatus.ERROR -> {
                itemView.progressBar.gone()
                itemView.btnResend.isEnabled = true
                itemView.btnResend.visible()
                itemView.ivPlay.gone()
            }
        }
    }

    interface Callback : ResendMessageCallback {
        fun onVideoMessageClicked(chatMessage: ChatMessageDto)
    }

    interface ActionCallback : ChatActionCallback
}