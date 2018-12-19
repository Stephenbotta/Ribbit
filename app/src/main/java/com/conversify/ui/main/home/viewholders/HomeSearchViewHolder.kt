package com.conversify.ui.main.home.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View

class HomeSearchViewHolder(itemView: View,
                           callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener { callback.onHomeSearchClicked() }
    }

    interface Callback {
        fun onHomeSearchClicked()
    }
}