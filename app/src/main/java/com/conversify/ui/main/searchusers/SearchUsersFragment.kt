package com.conversify.ui.main.searchusers

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.profile.ProfileActivity
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_search_users.*

class SearchUsersFragment : BaseFragment() {

    companion object {
        const val TAG = "SearchUsersFragment"
    }

    private val viewModel by lazy { ViewModelProviders.of(this)[SearchUsersViewModel::class.java] }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_search_users

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inItClasses()
    }

    private fun inItClasses() {
        val thumbnail = GlideApp.with(this).load(viewModel.getProfile().image?.thumbnail)
        GlideApp.with(this)
                .load(viewModel.getProfile().image?.original)
                .thumbnail(thumbnail)
                .into(ivProfilePic)
        ivProfilePic.setOnClickListener { startActivity(Intent(requireContext(), ProfileActivity::class.java)) }
    }


}