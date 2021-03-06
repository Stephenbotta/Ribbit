package com.ribbit.ui.people

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ribbit.R
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.people.UserCrossedDto
import com.ribbit.databinding.FragmentPeopleBinding
import com.ribbit.extensions.handleError
import com.ribbit.extensions.isNetworkActiveWithMessage
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.chat.ChatActivity
import com.ribbit.ui.people.details.PeopleDetailsActivity
import com.ribbit.utils.AppConstants
import com.ribbit.utils.GlideApp

class PeopleFragment : BaseFragment(), PeopleCallback {
    private lateinit var viewModel: PeopleViewModel
    private lateinit var binding: FragmentPeopleBinding
    private lateinit var adapter: PeopleAdapter

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
        binding.lifecycleOwner = this
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
                    val users = resource.data ?: emptyList()
                    binding.rvPeople.visibility = View.VISIBLE
                    adapter.displayCategories(users)
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

    override fun onClickItem(user: UserCrossedDto, isDetailShow: Boolean) {
        if (isDetailShow) {
            val intent = PeopleDetailsActivity.getStartIntent(requireContext(), user,
                    AppConstants.REQ_CODE_PEOPLE, user.crossedUser?.id ?: "")
            activity?.startActivity(intent)
        } else {
            val intent = ChatActivity.getStartIntentForIndividualChat(requireActivity(), user, AppConstants.REQ_CODE_INDIVIDUAL_CHAT)
            startActivityForResult(intent, AppConstants.REQ_CODE_INDIVIDUAL_CHAT)
        }
    }
}