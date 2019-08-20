package com.checkIt.ui.venues.join

import android.view.View
import android.view.ViewGroup
import com.checkIt.R
import com.checkIt.data.remote.models.chat.MemberDto
import com.checkIt.data.remote.models.venues.VenueDto
import com.checkIt.extensions.gone
import com.checkIt.extensions.inflate
import com.checkIt.extensions.toLatLng
import com.checkIt.extensions.visible
import com.checkIt.ui.venues.details.viewholder.VenueDetailsMemberViewHolder
import com.checkIt.utils.AppUtils
import com.checkIt.utils.DateTimeUtils
import com.checkIt.utils.GlideRequests
import com.checkIt.utils.MapUtils
import kotlinx.android.synthetic.main.item_join_venue_header.view.*

class JoinVenueDetailsAdapter(val glide: GlideRequests,
                              private val callback: Callback) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_MEMBER = 1
    }

    private val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> JoinVenueHeaderViewHolder(parent.inflate(R.layout.item_join_venue_header), glide)

            VIEW_TYPE_MEMBER -> VenueDetailsMemberViewHolder(parent.inflate(R.layout.item_venue_details_member), glide, callback)

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is JoinVenueHeaderViewHolder -> {
                if (item is VenueDto) {
                    holder.bind(item)
                }
            }

            is VenueDetailsMemberViewHolder -> {
                if (item is MemberDto) {
                    holder.bind(item)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]

        return when (item) {
            is VenueDto -> VIEW_TYPE_HEADER
            else -> VIEW_TYPE_MEMBER
        }
    }

    fun displayItems(items: List<Any>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    class JoinVenueHeaderViewHolder(itemView: View,
                                    private val glide: GlideRequests) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        init {
            itemView.btnDirections.setOnClickListener {
                val location = venue.venueLocation.toLatLng()
                MapUtils.openGoogleMaps(itemView.context, location.latitude, location.longitude)
            }
        }

        private lateinit var venue: VenueDto

        fun bind(venue: VenueDto) {
            this.venue = venue

            itemView.apply {
                glide.load(venue.imageUrl?.original)
                        .into(ivVenue)

                tvVenueName.text = venue.name

                tvTags.text = AppUtils.fixHashTags(venue.tags ?: emptyList()).joinToString(" ")
                tvDateAndTime.text = DateTimeUtils.formatVenueDateTime(venue.venueDateTime)
                tvLocationName.text = venue.locationName
                tvLocationAddress.text = venue.locationAddress

                // Show participants label only if count is non-zero
                val participantCount = venue.members?.size ?: 0
                if (participantCount == 0) {
                    tvLabelParticipants.gone()
                } else {
                    tvLabelParticipants.visible()
                }
            }
        }
    }

    interface Callback : VenueDetailsMemberViewHolder.Callback
}