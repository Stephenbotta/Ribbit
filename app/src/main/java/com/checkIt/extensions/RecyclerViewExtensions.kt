package com.checkIt.extensions

import androidx.recyclerview.widget.RecyclerView

fun androidx.recyclerview.widget.RecyclerView.ViewHolder.isValidPosition(): Boolean {
    return adapterPosition != androidx.recyclerview.widget.RecyclerView.NO_POSITION
}