package com.conversify.ui.creategroup.create.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View

class AddParticipantsViewHolder(itemView: View,
                                callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener { callback.onAddParticipantsClicked() }
    }

    interface Callback {
        fun onAddParticipantsClicked()
    }
}