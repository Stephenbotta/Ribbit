package com.checkIt.ui.main.searchusers

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.checkIt.R
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import com.checkIt.data.remote.models.people.UserCrossedDto
import com.checkIt.extensions.inflate
import com.checkIt.utils.GlideRequests
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.android.synthetic.main.item_search_user.view.*

class SearchUsersAdapter(private val glide: GlideRequests, private val callback: Callback) : RecyclerView.Adapter<SearchUsersAdapter.ViewHolder>() {

    private val userList = mutableListOf<ProfileDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUsersAdapter.ViewHolder {
        return ViewHolder(parent.inflate(R.layout.item_search_user), glide, callback)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindUserData(userList[position])
    }

    fun displayList(searchedUsersList: List<ProfileDto>) {
        userList.clear()
        userList.addAll(searchedUsersList)
        notifyDataSetChanged()
    }

    fun addToList(searchedUsersList: List<ProfileDto>) {
        val oldSize = userList.size
        userList.addAll(searchedUsersList)
        notifyItemRangeInserted(oldSize, userList.size - 1)
    }

    class ViewHolder(itemView: View, private val glide: GlideRequests, callback: Callback) : RecyclerView.ViewHolder(itemView) {
        private val context = itemView.context
        private val interestsAdapter = InterestsAdapter(context)

        init {
            val layoutManager = FlexboxLayoutManager(context)
            layoutManager.flexWrap = FlexWrap.WRAP
            itemView.rvMutualInterests.layoutManager = layoutManager
            itemView.rvMutualInterests.isNestedScrollingEnabled = false
            itemView.rvMutualInterests.adapter = interestsAdapter

            itemView.setOnClickListener {
                val user = UserCrossedDto(crossedUser = ProfileDto(fullName = profile.fullName, userName = profile.userName, image = profile.image,
                        id = profile.id),
                        conversationId = profile.conversationId)
                callback.openChatScreen(user, true)
            }

            itemView.fabChat.setOnClickListener {
                val user = UserCrossedDto(crossedUser = ProfileDto(fullName = profile.fullName, userName = profile.userName, image = profile.image,
                        id = profile.id),
                        conversationId = profile.conversationId)
                callback.openChatScreen(user, false)
            }
        }

        private lateinit var profile: ProfileDto

        fun bindUserData(profile: ProfileDto) {
            this.profile = profile
            glide.load(profile.image?.thumbnail)
                    .error(R.color.greyImageBackground)
                    .placeholder(R.color.greyImageBackground)
                    .into(itemView.ivProfilePic)

            itemView.tvUserName.text = profile.userName

            interestsAdapter.displayMutualInterests(profile.interests
                    ?: emptyList())
        }
    }

    interface Callback {
        fun openChatScreen(user: UserCrossedDto, isDetailShow: Boolean)
    }
}