package com.checkIt.extensions

import android.support.v7.widget.RecyclerView

fun RecyclerView.ViewHolder.isValidPosition(): Boolean {
    return adapterPosition != RecyclerView.NO_POSITION
}