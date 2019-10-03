package com.ribbit.extensions

fun androidx.recyclerview.widget.RecyclerView.ViewHolder.isValidPosition(): Boolean {
    return adapterPosition != androidx.recyclerview.widget.RecyclerView.NO_POSITION
}