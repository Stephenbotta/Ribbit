package com.ribbit.ui.createvenue

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ribbit.R
import com.ribbit.data.remote.models.Status
import com.ribbit.extensions.handleError
import com.ribbit.extensions.isNetworkActiveWithMessage
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.loginsignup.chooseinterests.ChooseInterestsViewModel
import com.ribbit.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_venue_categories.*

class VenueCategoriesFragment : BaseFragment() {
    companion object {
        const val TAG = "VenueCategoriesFragment"
    }

    private lateinit var interestsViewModel: ChooseInterestsViewModel
    private lateinit var adapter: VenueCategoriesAdapter

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_venue_categories

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        interestsViewModel = ViewModelProviders.of(this)[ChooseInterestsViewModel::class.java]
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