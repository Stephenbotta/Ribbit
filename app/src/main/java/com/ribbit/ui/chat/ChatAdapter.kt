package com.ribbit.ui.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ribbit.R
import com.ribbit.data.remote.ApiConstants
import com.ribbit.data.remote.models.chat.ChatDeleteDto
import com.ribbit.data.remote.models.chat.ChatMessageDto
import com.ribbit.data.remote.models.chat.MessageStatus
import com.ribbit.ui.chat.viewholders.ViewHolderChatImage
import com.ribbit.ui.chat.viewholders.ViewHolderChatText
import com.ribbit.ui.chat.viewholders.ViewHolderChatVideo
import com.ribbit.utils.GlideApp

class ChatAdapter(context: Context, private val callback: Callback,
                  private val actionCallback: ActionCallback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_TEXT_LEFT = 0
        private const val TYPE_TEXT_RIGHT = 1
        private const val TYPE_IMAGE_LEFT = 2
        private const val TYPE_IMAGE_RIGHT = 3
        private const val TYPE_VIDEO_LEFT = 4
        private const val TYPE_VIDEO_RIGHT = 5
    }

    private val glide = GlideApp.with(context)
    private val inflater = LayoutInflater.from(context)
    private val messages = mutableListOf<ChatMessageDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TEXT_LEFT -> {
                ViewHolderChatText(inflater.inflate(R.layout.item_chat_text_left, parent, false), glide, actionCallback)
            }

            TYPE_TEXT_RIGHT -> {
                ViewHolderChatText(inflater.inflate(R.layout.item_chat_text_right, parent, false), glide, actionCallback)
            }

            TYPE_IMAGE_LEFT -> {
                ViewHolderChatImage(inflater.inflate(R.layout.item_chat_image_left, parent, false), glide, callback, actionCallback)
            }

            TYPE_IMAGE_RIGHT -> {
                ViewHolderChatImage(inflater.inflate(R.layout.item_chat_image_right, parent, false), glide, callback, actionCallback)
            }

            TYPE_VIDEO_LEFT -> {
                ViewHolderChatVideo(inflater.inflate(R.layout.item_chat_video_left, parent, false), glide, callback, actionCallback)
            }

            TYPE_VIDEO_RIGHT -> {
                ViewHolderChatVideo(inflater.inflate(R.layout.item_chat_video_right, parent, false), glide, callback, actionCallback)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]

        return when (message.details?.type) {
            ApiConstants.MESSAGE_TYPE_TEXT -> {
                if (message.ownMessage) {
                    TYPE_TEXT_RIGHT
                } else {
                    TYPE_TEXT_LEFT
                }
            }

            ApiConstants.MESSAGE_TYPE_IMAGE, ApiConstants.MESSAGE_TYPE_GIF -> {
                if (message.ownMessage) {
                    TYPE_IMAGE_RIGHT
                } else {
                    TYPE_IMAGE_LEFT
                }
            }

            ApiConstants.MESSAGE_TYPE_VIDEO -> {
                if (message.ownMessage) {
                    TYPE_VIDEO_RIGHT
                } else {
                    TYPE_VIDEO_LEFT
                }
            }

            else -> TYPE_TEXT_LEFT
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val aboveMessage = messages.getOrNull(position - 1)
        val currentMessage = messages[position]
        ChatHelper.updateCurrentMessage(position, currentMessage, aboveMessage)

        when (holder) {
            is ViewHolderChatText -> holder.bind(currentMessage)
            is ViewHolderChatImage -> holder.bind(currentMessage)
            is ViewHolderChatVideo -> holder.bind(currentMessage)
        }
    }

    fun addNewMessage(message: ChatMessageDto) {
        messages.add(message)
        notifyItemInserted(itemCount - 1)
    }

    fun addNewMessageMultiple(messages: List<ChatMessageDto>) {
        val oldListSize = this.messages.size
        this.messages.addAll(messages)
        notifyItemRangeInserted(oldListSize, messages.size)
    }

    fun addOldMessages(messages: List<ChatMessageDto>) {
        this.messages.addAll(0, messages)
        notifyItemRangeInserted(0, messages.size)
        notifyItemChanged(messages.size)
    }

    fun removeMsgPosition(chatMessage: ChatMessageDto) {
        val position = messages.indexOfFirst { chatMessage.id == it.id }
        if (position != -1) {
            messages.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getMsgDetail(position: Int): ChatMessageDto {
        return this.messages[position]
    }

    fun updateMessageStatus(localId: String, messageStatus: MessageStatus) {
        val totalMessages = messages.size
        if (totalMessages == 0) return

        // Find the message from the last and update message status
        for (index in totalMessages - 1 downTo 0) {
            if (messages[index].localId == localId) {
                messages[index].messageStatus = messageStatus
                notifyItemChanged(index)
                break
            }
        }
    }

    fun removeMessage(message: ChatDeleteDto) {
        val position = messages.indexOfFirst { it.id == message.messageId }
        if (position != -1) {
            messages.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    interface Callback : ViewHolderChatImage.Callback, ViewHolderChatVideo.Callback

    interface ActionCallback : ViewHolderChatText.ActionCallback, ViewHolderChatImage.ActionCallback, ViewHolderChatVideo.ActionCallback
}