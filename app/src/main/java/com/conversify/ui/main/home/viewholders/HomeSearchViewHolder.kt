package com.conversify.ui.main.home.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_home_search.view.*

class HomeSearchViewHolder(itemView: View,
                           callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.ivSearch.setOnClickListener { callback.onHomeSearchClicked() }
    }

    interface Callback {
        fun onHomeSearchClicked()
    }
}