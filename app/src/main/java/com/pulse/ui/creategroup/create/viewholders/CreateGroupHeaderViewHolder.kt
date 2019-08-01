package com.pulse.ui.creategroup.create.viewholders

import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.CompoundButton
import com.pulse.R
import com.pulse.data.remote.models.groups.CreateGroupHeaderDto
import com.pulse.extensions.hideKeyboard
import com.pulse.utils.GlideRequests
import kotlinx.android.synthetic.main.item_create_group_header.view.*

class CreateGroupHeaderViewHolder(itemView: View,
                                  private val glide: GlideRequests,
                                  private val callback: Callback) : RecyclerView.ViewHolder(itemView), CompoundButton.OnCheckedChangeListener {
    init {
        itemView.ivGroup.setOnClickListener {
            callback.onGroupImageClicked()
        }

        itemView.etGroupTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                header.groupTitle = s?.toString()
                callback.onGroupTitleTextChanged()
            }
        })

        itemView.etGroupTitle.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                view.hideKeyboard()
            }
        }

        itemView.etDescription.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                header.description = s?.toString()
                callback.onGroupDescriptionTextChanged()
            }
        })

        itemView.etDescription.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                view.hideKeyboard()
            }
        }

        itemView.switchPrivateGroup.setOnCheckedChangeListener(this)
    }

    private lateinit var header: CreateGroupHeaderDto

    fun bind(header: CreateGroupHeaderDto) {
        this.header = header

        itemView.apply {
            tvCategory.text = header.categoryName
            if (header.selectedGroupImageFile != null) {
                glide.load(header.selectedGroupImageFile)
                        .into(ivGroup)
            }
            etGroupTitle.setText(header.groupTitle)
            etDescription.setText(header.description)
            switchPrivateGroup.isChecked = header.isPrivate
            tvLabelMembers.text = context.getString(R.string.venue_details_label_members_with_count, header.memberCount)
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        header.isPrivate = isChecked
    }

    interface Callback {
        fun onGroupImageClicked()
        fun onGroupTitleTextChanged()
        fun onGroupDescriptionTextChanged()
    }
}