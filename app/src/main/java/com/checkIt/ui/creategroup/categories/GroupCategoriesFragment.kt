package com.checkIt.ui.creategroup.categories

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.checkIt.R
import com.checkIt.data.remote.models.Status
import com.checkIt.extensions.handleError
import com.checkIt.extensions.isNetworkActiveWithMessage
import com.checkIt.ui.base.BaseFragment
import com.checkIt.ui.creategroup.create.CreateGroupFragment
import com.checkIt.ui.loginsignup.chooseinterests.ChooseInterestsViewModel
import com.checkIt.utils.GlideApp
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