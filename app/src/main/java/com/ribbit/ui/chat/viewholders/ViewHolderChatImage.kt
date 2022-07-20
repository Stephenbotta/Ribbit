package com.ribbit.ui.chat.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ribbit.data.local.UserManager
import com.ribbit.data.remote.models.chat.ChatMessageDto
import com.ribbit.data.remote.models.chat.MessageStatus
import com.ribbit.extensions.gone
import com.ribbit.extensions.isNetworkActiveWithMessage
import com.ribbit.extensions.visible
import com.ribbit.ui.chat.ChatActionCallback
import com.ribbit.ui.chat.ResendMessageCallback
import com.ribbit.utils.DialogsUtil
import com.ribbit.utils.GlideRequests
import kotlinx.android.synthetic.main.item_chat_image_left.view.*

class ViewHolderChatImage(itemView: View, private val glide: GlideRequests, private val callback: Callback,
                          private val actionCallback: ActionCallback) : ViewHolderChat(itemView) {
    init {
        itemView.btnResend.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION && itemView.context.isNetworkActiveWithMessage()) {
                chatMessage.messageStatus = MessageStatus.SENDING
                updateMessageStatus(chatMessage.messageStatus)
                callback.onResendMessageClicked(chatMessage)
            }
        }

        itemView.ivImage.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                callback.onImageMessageClicked(chatMessage)
            }
        }

        itemView.ivImage.setOnLongClickListener {
            if (chatMessage.sender?.id == UserManager.getUserId())
                DialogsUtil.openAlertDialog(itemView.context, chatMessage, actionCallback)
            true
        }
    }

    private lateinit var chatMessage: ChatMessageDto

    fun bind(chatMessage: ChatMessageDto) {
        this.chatMessage = chatMessage
        displayCommonValues(chatMessage, glide)

        // Display local file if it exists otherwise display the image from the url
        val image: Any? = if (chatMessage.localFile != null) {
            chatMessage.localFile
        } else {
            chatMessage.details?.image?.original
        }
        glide.load(image)
                .into(itemView.ivImage)

        updateMessageStatus(chatMessage.messageStatus)
    }

    private fun updateMessageStatus(status: MessageStatus) {
        when (status) {
            MessageStatus.SENDING -> {
                itemView.progressBar.visible()
                itemView.btnResend.isEnabled = false
                itemView.btnResend.gone()
            }

            MessageStatus.SENT -> {
                itemView.progressBar.gone()
                itemView.btnResend.isEnabled = false
                itemView.btnResend.gone()
            }

            MessageStatus.ERROR -> {
                itemView.progressBar.gone()
                itemView.btnResend.isEnabled = true
                itemView.btnResend.visible()
            }
        }
    }

    interface Callback : ResendMessageCallback {
        fun onImageMessageClicked(chatMessage: ChatMessageDto)
    }

    interface ActionCallback : ChatActionCallback
}