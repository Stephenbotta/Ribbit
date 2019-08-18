package com.checkIt.ui.groups.topics

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.checkIt.R
import com.checkIt.data.remote.models.Status
import com.checkIt.extensions.handleError
import com.checkIt.ui.loginsignup.chooseinterests.ChooseInterestsViewModel
import com.checkIt.utils.AppConstants
import com.checkIt.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_group_topics.*

class GroupTopicsFragment : DialogFragment() {
    companion object {
        const val TAG = "GroupTopicsFragment"
    }

    private lateinit var interestsViewModel: ChooseInterestsViewModel
    private lateinit var topicsAdapter: GroupTopicsAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogFragmentAnimation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        interestsViewModel = ViewModelProviders.of(this)[ChooseInterestsViewModel::class.java]
        topicsAdapter = GroupTopicsAdapter(GlideApp.with(this)) { topic ->
            val data = Intent()
            data.putExtra(AppConstants.EXTRA_INTEREST, topic)
            targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_group_topics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvTopics.adapter = topicsAdapter
        observeChanges()
        viewEmpty.setOnClickListener { dismiss() }
        interestsViewModel.getInterests()
    }

    private fun observeChanges() {
        interestsViewModel.interests.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    topicsAdapter.displayTopics(resource.data ?: emptyList())
                }

                Status.ERROR -> {
                    handleError(resource.error)
                }

                Status.LOADING -> {
                }
            }
        })
    }
}