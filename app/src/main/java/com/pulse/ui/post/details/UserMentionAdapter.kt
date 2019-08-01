package com.pulse.ui.post.details

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.pulse.R
import com.pulse.data.remote.models.loginsignup.ProfileDto
import com.pulse.extensions.inflate
import com.pulse.extensions.isValidPosition
import com.pulse.utils.GlideRequests
import kotlinx.android.synthetic.main.item_user_mention.view.*

class UserMentionAdapter(private val glide: GlideRequests,
                         private val callback: Callback) : RecyclerView.Adapter<UserMentionAdapter.UserMentionViewHolder>() {
    private val users = mutableListOf<ProfileDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserMentionViewHolder {
        return UserMentionViewHolder(parent.inflate(R.layout.item_user_mention), glide, callback)
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: UserMentionViewHolder, position: Int) {
        holder.bind(users[position])
    }

    fun displayMentions(users: List<ProfileDto>) {
        this.users.clear()
        this.users.addAll(users)
        notifyDataSetChanged()
    }

    class UserMentionViewHolder(itemView: View,
                                private val glide: GlideRequests,
                                callback: Callback) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                if (isValidPosition()) {
                    callback.onUserMentionSuggestionClicked(user)
                }
            }
            itemView.setOnLongClickListener {
//                callback.onUserMentionLongPressed(user)
                true
            }
        }

        private lateinit var user: ProfileDto

        fun bind(user: ProfileDto) {
            this.user = user

            glide.load(user.image?.thumbnail)
                    .into(itemView.ivProfile)
            itemView.tvUserName.text = user.userName
        }
    }

    interface Callback {
        fun onUserMentionSuggestionClicked(user: ProfileDto)
        fun onUserMentionLongPressed(user: ProfileDto)
    }
}