package com.ribbit.ui.creategroup.create.viewholders

import android.view.View

class AddParticipantsViewHolder(itemView: View,
                                callback: Callback) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener { callback.onAddParticipantsClicked() }
    }

    interface Callback {
        fun onAddParticipantsClicked()
    }
}