package com.conversify.ui.creategroup.create.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CompoundButton
import com.conversify.data.remote.models.groups.CreateGroupHeaderDto
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_create_group_header.view.*

class CreateGroupHeaderViewHolder(itemView: View,
                                  private val glide: GlideRequests,
                                  private val callback: Callback) : RecyclerView.ViewHolder(itemView), CompoundButton.OnCheckedChangeListener {
    init {
        itemView.ivGroup.setOnClickListener {
            callback.onGroupImageClicked()
        }
    }

    private lateinit var header: CreateGroupHeaderDto

    fun bind(header: CreateGroupHeaderDto) {
        this.header = header

        itemView.apply {
            tvCategory.text = header.categoryName
            glide.load(header.groupImage)
                    .into(ivGroup)
            etGroupTitle.setText(header.groupTitle)
            switchPrivateGroup.isChecked = header.isPrivate
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        header.isPrivate = isChecked
    }

    interface Callback {
        fun onGroupImageClicked()
    }
}