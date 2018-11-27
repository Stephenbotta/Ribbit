package com.conversify.ui.venues.chat

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.models.chat.ChatMessageDto
import com.conversify.data.remote.models.chat.MessageStatus
import com.conversify.ui.venues.chat.viewholders.ViewHolderChatText
import com.conversify.utils.GlideApp

class ChatAdapter(context: Context,
                  private val callback: Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_TEXT_LEFT = 0
        private const val TYPE_TEXT_RIGHT = 1
    }

    private val glide = GlideApp.with(context)
    private val inflater = LayoutInflater.from(context)
    private val messages = mutableListOf<ChatMessageDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TEXT_LEFT -> {
                ViewHolderChatText(inflater.inflate(R.layout.item_chat_text_left, parent, false), glide)
            }

            TYPE_TEXT_RIGHT -> {
                ViewHolderChatText(inflater.inflate(R.layout.item_chat_text_right, parent, false), glide)
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

            else -> TYPE_TEXT_LEFT
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val aboveMessage = messages.getOrNull(position - 1)
        val currentMessage = messages[position]
        ChatHelper.updateCurrentMessage(position, currentMessage, aboveMessage)

        when (holder) {
            is ViewHolderChatText -> holder.bind(currentMessage)
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

    interface Callback
}