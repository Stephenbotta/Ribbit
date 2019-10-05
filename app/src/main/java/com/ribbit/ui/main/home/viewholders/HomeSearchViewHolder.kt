package com.ribbit.ui.main.home.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_home_search.view.*

class HomeSearchViewHolder(itemView: View,
                           callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.ivSearch.setOnClickListener { callback.onHomeSearchClicked() }
        itemView.ivNotification.setOnClickListener { callback.onHomeNotificationClicked() }
    }

    interface Callback {
        fun onHomeSearchClicked()
        fun onHomeNotificationClicked()
    }
}