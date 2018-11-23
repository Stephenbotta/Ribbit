package com.conversify.ui.createvenue

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseFragment
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_venue_categories.*

class VenueCategoriesFragment : BaseFragment() {
    companion object {
        const val TAG = "VenueCategoriesFragment"
    }

    private lateinit var viewModel: CreateVenueViewModel
    private lateinit var adapter: VenueCategoriesAdapter

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_venue_categories

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this)[CreateVenueViewModel::class.java]
        adapter = VenueCategoriesAdapter(GlideApp.with(this)) {
            val fragment = CreateVenueFragment.newInstance(it)
            fragmentManager?.apply {
                beginTransaction()
                        .setCustomAnimations(R.anim.parallax_right_in, R.anim.parallax_left_out,
                                R.anim.parallax_left_in, R.anim.parallax_right_out)
                        .replace(R.id.flContainer, fragment, CreateVenueFragment.TAG)
                        .addToBackStack(null)
                        .commit()
            }
        }
        rvVenueCategories.adapter = adapter
        observeChanges()
        getCategories()
    }

    private fun observeChanges() {
        viewModel.interests.observe(this, Observer { resource ->
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
        val shouldFetchInterests = viewModel.hasInterests() || isNetworkActiveWithMessage()
        if (shouldFetchInterests) {
            viewModel.getInterests()
        }
    }
}