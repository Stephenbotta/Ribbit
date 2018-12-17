package com.conversify.ui.creategroup.categories

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.creategroup.create.CreateGroupFragment
import com.conversify.ui.loginsignup.chooseinterests.ChooseInterestsViewModel
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_group_categories.*

class GroupCategoriesFragment : BaseFragment() {
    companion object {
        const val TAG = "GroupCategoriesFragment"
    }

    private lateinit var interestsViewModel: ChooseInterestsViewModel
    private lateinit var adapter: GroupCategoriesAdapter

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_group_categories

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        interestsViewModel = ViewModelProviders.of(this)[ChooseInterestsViewModel::class.java]
        adapter = GroupCategoriesAdapter(GlideApp.with(this)) {
            val fragment = CreateGroupFragment.newInstance(it)
            fragmentManager?.apply {
                beginTransaction()
                        .setCustomAnimations(R.anim.parallax_right_in, R.anim.parallax_left_out,
                                R.anim.parallax_left_in, R.anim.parallax_right_out)
                        .replace(R.id.flContainer, fragment, CreateGroupFragment.TAG)
                        .addToBackStack(null)
                        .commit()
            }
        }
        rvGroupCategories.adapter = adapter
        observeChanges()
        getCategories()
    }

    private fun observeChanges() {
        interestsViewModel.interests.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    val interests = resource.data ?: emptyList()
                    adapter.displayCategories(interests)
                }

                Status.ERROR -> {
                    handleError(resource.error)
                }

                Status.LOADING -> {
                }
            }
        })
    }

    private fun getCategories() {
        val shouldFetchInterests = interestsViewModel.hasInterests() || isNetworkActiveWithMessage()
        if (shouldFetchInterests) {
            interestsViewModel.getInterests()
        }
    }
}