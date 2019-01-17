package com.conversify.ui.main.chats

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.chat.ChatListingDto
import com.conversify.data.remote.models.venues.VenueCategoriesHeader
import com.conversify.extensions.inflate
import com.conversify.utils.DateTimeUtils
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_chat_listing.view.*


/**
 * Created by Manish Bhargav on 14/1/19
 */
class ChatListCommonAdapter(private val glide: GlideRequests, private val callback: ChatListCallback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<Any>(VenueCategoriesHeader)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolderPeoples(parent.inflate(R.layout.item_chat_listing), glide, callback)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is ViewHolderPeoples -> {
                if (item is ChatListingDto) {
                    holder.bind(item)
                }
            }
        }
    }

    fun displayCategories(peoples: List<Any>) {
        items.clear()
        items.addAll(peoples)
        notifyDataSetChanged()
    }

    class ViewHolderPeoples(itemView: View,
                            private val glide: GlideRequests, private val callback: ChatListCallback) : RecyclerView.ViewHolder(itemView) {
        private lateinit var peoples: ChatListingDto

        init {
            itemView.setOnClickListener { callback.onClickItem(adapterPosition) }
        }

        fun bind(category: ChatListingDto) {
            this.peoples = category

            glide.load(category.profile?.image?.thumbnail)
                    .error(R.color.greyImageBackground)
                    .placeholder(R.color.greyImageBackground)
                    .into(itemView.ivProfilePic)
            itemView.tvTimeAgo.text = DateTimeUtils.formatPeopleRecentTime(category.createdDate)

            itemView.tvTitle.text = category.profile?.userName

            if (category.lastChatDetails?.type.equals("TEXT")) {
                itemView.tvChat.text = category.lastChatDetails?.message
                itemView.tvChat.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
            } else if (category.lastChatDetails?.type.equals("IMAGE")) {
                itemView.tvChat.text = itemView.context.getString(R.string.chat_listing_photo_last_message_for_chat)
                itemView.tvChat.compoundDrawablePadding = 8
                itemView.tvChat.setCompoundDrawablesWithIntrinsicBounds(itemView.context.getDrawable(R.drawable.ic_photo_camera), null, null, null)
            } else if (category.lastChatDetails?.type.equals("VIDEO")) {
                itemView.tvChat.text = itemView.context.getString(R.string.chat_listing_video_last_message_for_chat)
                itemView.tvChat.setCompoundDrawablesWithIntrinsicBounds(itemView.context.getDrawable(R.drawable.ic_video), null, null, null)
                itemView.tvChat.compoundDrawablePadding = 8
            }
        }
    }
}