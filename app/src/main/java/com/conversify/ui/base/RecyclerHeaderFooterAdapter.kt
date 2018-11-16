package com.conversify.ui.base

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

abstract class RecyclerHeaderFooterAdapter<T>(private var showHeader: Boolean,
                                              private var showFooter: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_FOOTER = 1
    }

    private val items = mutableListOf<T>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> createHeaderViewHolder()
            TYPE_FOOTER -> createFooterViewHolder()
            else -> createItemViewHolder(viewType)
        }
    }

    override fun getItemCount(): Int {
        var count = items.size
        if (showHeader) {
            ++count
        }
        if (showFooter) {
            ++count
        }
        return count
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            isHeaderPosition(position) -> TYPE_HEADER
            isFooterPosition(position) -> TYPE_FOOTER
            else -> getListItemViewType(position)
        }
    }

    fun showHeader() {
        showHeader = true
        notifyItemInserted(0)
    }

    fun hideHeader() {
        showHeader = false
        notifyItemRemoved(0)
    }

    fun showFooter() {
        showFooter = true
        notifyItemInserted(itemCount - 1)
    }

    fun hideFooter() {
        showFooter = false
        notifyItemRemoved(itemCount - 1)
    }

    private fun isHeaderPosition(position: Int): Boolean {
        return showHeader && position == 0
    }

    private fun isFooterPosition(position: Int): Boolean {
        return showFooter && position == itemCount - 1
    }

    abstract fun createHeaderViewHolder(): RecyclerView.ViewHolder
    abstract fun createItemViewHolder(viewType: Int): RecyclerView.ViewHolder
    abstract fun createFooterViewHolder(): RecyclerView.ViewHolder
    abstract fun getListItemViewType(position: Int): Int
}