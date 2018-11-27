package com.conversify.ui.groups

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.conversify.R
import com.conversify.ui.base.BaseFragment
import com.leinardi.android.speeddial.SpeedDialActionItem
import kotlinx.android.synthetic.main.fragment_groups.*

class GroupsFragment : BaseFragment() {
    companion object {
        const val TAG = "GroupsFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_groups

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGroupsFab()
    }

    private fun setupGroupsFab() {
        val colorWhite = ContextCompat.getColor(requireActivity(), R.color.white)
        val colorPrimary = ContextCompat.getColor(requireActivity(), R.color.colorPrimary)
        val colorTransparent = ContextCompat.getColor(requireActivity(), R.color.transparent)

        fabGroups.addActionItem(SpeedDialActionItem.Builder(R.id.fabAddGroup, R.drawable.ic_plus_white)
                .setFabBackgroundColor(colorWhite)
                .setFabImageTintColor(colorPrimary)
                .setLabel(R.string.groups_label_add_new_group)
                .setLabelColor(colorWhite)
                .setLabelBackgroundColor(colorTransparent)
                .setLabelClickable(false)
                .create())

        fabGroups.addActionItem(SpeedDialActionItem.Builder(R.id.fabTopics, R.drawable.ic_grid)
                .setFabBackgroundColor(colorWhite)
                .setFabImageTintColor(colorPrimary)
                .setLabel(R.string.groups_label_topics)
                .setLabelColor(colorWhite)
                .setLabelBackgroundColor(colorTransparent)
                .setLabelClickable(false)
                .create())

        fabGroups.setOnActionSelectedListener { item ->
            return@setOnActionSelectedListener when (item.id) {
                R.id.fabAddGroup -> {
                    false
                }

                R.id.fabTopics -> {
                    false
                }

                else -> false
            }
        }
    }
}