package com.conversify.ui.people

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.people.UserCrossedDto
import com.conversify.databinding.FragmentPeopleBinding
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.shortToast
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.people.details.PeopleDetailsActivity
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp

class PeopleFragment : BaseFragment(), PeopleCallback {

    private lateinit var viewModel: PeopleViewModel
    private lateinit var binding: FragmentPeopleBinding
    private lateinit var adapter: PeopleAdapter
    private lateinit var items: List<Any>

    companion object {
        const val TAG = "PeopleFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_people

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, getFragmentLayoutResId(), container, false)
        binding.view = this
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inItClasses()
        observeChanges()
        getCrossedPeople()
    }

    private fun inItClasses() {
        viewModel = ViewModelProviders.of(this).get(PeopleViewModel::class.java)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        adapter = PeopleAdapter(GlideApp.with(this), this)
        binding.rvPeople.adapter = adapter
        binding.rvPeople.visibility = View.GONE
        binding.swipeRefreshLayout.setOnRefreshListener { getCrossedPeople() }
    }

    private fun observeChanges() {

        viewModel.crossedPeople.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {

                Status.SUCCESS -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    items = resource.data ?: emptyList()
                    binding.rvPeople.visibility = View.VISIBLE
                    adapter.displayCategories(items)

                }

                Status.ERROR -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    binding.swipeRefreshLayout.isRefreshing = true
                }
            }

        })
    }

    private fun getCrossedPeople() {
        if (isNetworkActiveWithMessage()) {
            viewModel.getCrossedPeople()
        } else {
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onClickItem(position: Int, isDetailShow: Boolean) {
        val item = items[position]
        if (isDetailShow) {
            if (item is UserCrossedDto) {
                val intent = Intent(activity, PeopleDetailsActivity::class.java)
                intent.putExtra(AppConstants.INTENT_PEOPLE_DETAILS_USER_ID, item.crossedUser?.id)
                activity?.startActivity(intent)
            }
        } else {
            if (item is UserCrossedDto)
                if (!item.conversationId.isNullOrEmpty())
                    requireActivity().shortToast(item.conversationId)
        }

    }

    override fun onClickItem() {
    }

    override fun onClickItem(position: Int) {
    }

}