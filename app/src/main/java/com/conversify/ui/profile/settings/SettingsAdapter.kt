package com.conversify.ui.profile.settings

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.people.GetPeopleResponse
import com.conversify.data.remote.models.people.UserCrossedDto
import com.conversify.data.remote.models.venues.VenueCategoriesHeader
import com.conversify.extensions.inflate
import com.conversify.extensions.invisible
import com.conversify.extensions.visible
import com.conversify.ui.people.PeopleCallback
import com.conversify.utils.DateTimeUtils
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_people.view.*
import kotlinx.android.synthetic.main.item_people_location.view.*

/**
 * Created by Manish Bhargav
 */
class SettingsAdapter(private val glide: GlideRequests, private val callback: PeopleCallback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_PLACES = 0
        private const val TYPE_PEOPLES = 1
    }

    private val items = mutableListOf<Any>(VenueCategoriesHeader)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_PLACES) {
            ViewHolderPlaces(parent.inflate(R.layout.item_people_location), callback)
        } else {
            ViewHolderPeoples(parent.inflate(R.layout.item_people), glide, callback)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is ViewHolderPeoples -> {
                if (item is UserCrossedDto) {
                    holder.bind(item)
                }
            }

            is ViewHolderPlaces -> {
                if (item is GetPeopleResponse) {
                    holder.bind(item)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is GetPeopleResponse) {
            TYPE_PLACES
        } else {
            TYPE_PEOPLES
        }
    }

    fun displayCategories(peoples: List<Any>) {
        items.clear()
        items.addAll(peoples)
        notifyDataSetChanged()
    }

    class ViewHolderPlaces(itemView: View, private val callback: PeopleCallback) : RecyclerView.ViewHolder(itemView) {

        private lateinit var places: GetPeopleResponse

        init {
            itemView.setOnClickListener { }
        }

        fun bind(places: GetPeopleResponse) {
            this.places = places
            itemView.tvDate.text = DateTimeUtils.formatPeopleLocation(places.timestamp)
            itemView.tvLocationName.text = places.locationName
            itemView.tvLocationAddress.text = places.locationAddress
        }

    }

    class ViewHolderPeoples(itemView: View,
                            private val glide: GlideRequests, private val callback: PeopleCallback) : RecyclerView.ViewHolder(itemView) {
        private lateinit var peoples: UserCrossedDto

        init {
            itemView.setOnClickListener { callback.onClickItem(adapterPosition, true) }
            itemView.fabChat.setOnClickListener { callback.onClickItem(adapterPosition, false) }
        }

        fun bind(category: UserCrossedDto) {
            this.peoples = category

            glide.load(category.crossedUser?.image?.thumbnail)
                    .error(R.color.greyImageBackground)
                    .placeholder(R.color.greyImageBackground)
                    .into(itemView.ivProfilePic)
            itemView.tvTime.text = DateTimeUtils.formatPeopleRecentTime(category.time)

            itemView.tvUserName.text = if (category.crossedUser?.age == null) {
                category.crossedUser?.fullName
            } else {
                itemView.context.getString(R.string.profile_label_name_with_age, category.crossedUser.fullName, category.crossedUser.age)
            }
            if (category.crossedUser?.designation.isNullOrBlank() || category.crossedUser?.company.isNullOrBlank()) {
                itemView.tvUserDesignation.invisible()
            } else {
                itemView.tvUserDesignation.visible()
                itemView.tvUserDesignation.text = itemView.context.getString(R.string.profile_label_designation_at_company, category.crossedUser?.designation, category.crossedUser?.company)
            }


            itemView.tvUserName.text = category.crossedUser?.userName
            itemView.tvUserDesignation.text = category.crossedUser?.designation
        }
    }
}