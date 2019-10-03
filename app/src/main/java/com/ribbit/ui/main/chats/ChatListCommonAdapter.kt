package com.ribbit.ui.main.chats

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ribbit.R
import com.ribbit.data.remote.models.chat.ChatListingDto
import com.ribbit.extensions.inflate
import com.ribbit.utils.DateTimeUtils
import com.ribbit.utils.GlideRequests
import kotlinx.android.synthetic.main.item_chat_listing.view.*

class ChatListCommonAdapter(private val glide: GlideRequests, private val callback: ChatListCallback) : RecyclerView.Adapter<ChatListCommonAdapter.ViewHolderPeoples>() {
    private val peoples = ArrayList<ChatListingDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderPeoples {
        return ViewHolderPeoples(parent.inflate(R.layout.item_chat_listing), glide, callback)
    }

    override fun getItemCount(): Int = peoples.size

    override fun onBindViewHolder(holder: ViewHolderPeoples, position: Int) {
        holder.bind(peoples[position])
    }

    fun displayCategories(peoples: List<ChatListingDto>) {
        this.peoples.clear()
        this.peoples.addAll(peoples)
        notifyDataSetChanged()
    }

    class ViewHolderPeoples(itemView: View, private val glide: GlideRequests,
                            private val callback: ChatListCallback) : RecyclerView.ViewHolder(itemView) {
        private lateinit var chat: ChatListingDto

        init {
            itemView.setOnClickListener { callback.onClickItem(chat) }
        }

        fun bind(chat: ChatListingDto) {
            this.chat = chat

            glide.load(chat.profile?.image?.thumbnail)
                    .error(R.color.greyImageBackground)
                    .placeholder(R.color.greyImageBackground)
                    .into(itemView.ivProfilePic)
            itemView.tvTimeAgo.text = DateTimeUtils.formatPeopleRecentTime(chat.createdDate)

            itemView.tvTitle.text = chat.profile?.userName

            when {
                chat.lastChatDetails?.type.equals("TEXT") -> {
                    itemView.tvChat.text = chat.lastChatDetails?.message
                    itemView.tvChat.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
                }
                chat.lastChatDetails?.type.equals("GIF") -> {
                    itemView.tvChat.text = itemView.context.getString(R.string.chat_listing_gif_last_message_for_chat)
                    itemView.tvChat.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
                }
                chat.lastChatDetails?.type.equals("IMAGE") -> {
                    itemView.tvChat.text = itemView.context.getString(R.string.chat_listing_photo_last_message_for_chat)
                    itemView.tvChat.compoundDrawablePadding = 8
                    itemView.tvChat.setCompoundDrawablesWithIntrinsicBounds(itemView.context.getDrawable(R.drawable.ic_photo_camera), null, null, null)
                }
                chat.lastChatDetails?.type.equals("VIDEO") -> {
                    itemView.tvChat.text = itemView.context.getString(R.string.chat_listing_video_last_message_for_chat)
                    itemView.tvChat.setCompoundDrawablesWithIntrinsicBounds(itemView.context.getDrawable(R.drawable.ic_video), null, null, null)
                    itemView.tvChat.compoundDrawablePadding = 8
                }
            }
        }
    }
}