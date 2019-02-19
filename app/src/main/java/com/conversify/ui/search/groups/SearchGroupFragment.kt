package com.conversify.ui.search.groups

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.venues.YourVenuesDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActive
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.longToast
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.custom.LoadingDialog
import com.conversify.ui.groups.groupposts.GroupPostsActivity
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_search_group.*


class SearchGroupFragment : BaseFragment(), SearchGroupAdapter.Callback {

    companion object {
        const val TAG = "SearchGroupFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_search_group

    private lateinit var viewModel: SearchGroupViewModel
    private lateinit var adapter: SearchGroupAdapter
    private lateinit var loadingDialog: LoadingDialog
    private var search = ""
    private var adapterPosition: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SearchGroupViewModel::class.java)
        adapter = SearchGroupAdapter(GlideApp.with(this), this)
        loadingDialog = LoadingDialog(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeChanges()
        setupHomeRecycler()
        getGroupSearch()
    }

    private fun observeChanges() {
        viewModel.groupSearch.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
//                    loadingDialog.setLoading(false)
                    val data = resource.data?.result ?: emptyList()
                    val firstPage = resource.data?.isFirstPage ?: true
                    val items = mutableListOf<Any>()
                    if (firstPage)
                        items.add(YourVenuesDto)
                    items.addAll(data)
                    if (firstPage) {
                        adapter.displayItems(items)
                    } else {
                        adapter.addMoreItems(items)
                    }
                }

                Status.ERROR -> {
//                    loadingDialog.setLoading(false)
                    handleError(resource.error)
                }

                Status.LOADING -> {
//                    loadingDialog.setLoading(true)
                }
            }
        })

        viewModel.joinGroup.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)

                    resource.data.let { group ->
                        if (group?.isPrivate == true) {
                            requireActivity().longToast(R.string.venues_message_notification_sent_to_admin)
                            getGroupSearch()
                        } else {
                            val items = adapter.getUpdatedList()
                            items.set(adapterPosition!!, group!!)
                            groupDetails(group)
                        }
                    }
                }

                Status.ERROR -> {
                    loadingDialog.setLoading(false)
                    handleError(resource.error)
                }

                Status.LOADING -> loadingDialog.setLoading(true)
            }
        })
    }

    private fun groupDetails(group: GroupDto) {
        GroupPostsActivity.start(requireActivity(), group)
        adapter.notifyDataSetChanged()
    }

    private fun getGroupSearch(showLoading: Boolean = true) {
        if (isNetworkActiveWithMessage()) {
            viewModel.getGroupSearch(showLoading, search)
        }
    }

    private fun setupHomeRecycler() {
        rvGroupSearch.adapter = adapter
        (rvGroupSearch.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        rvGroupSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1) && viewModel.validForPaging() && isNetworkActive()) {
                    viewModel.getGroupSearch(false, search)
                }
            }
        })
    }

    fun search(query: String) {
        this.search = query
        getGroupSearch()
    }

    override fun onClick(position: Int, group: GroupDto) {
        val items = adapter.getUpdatedList()
        val item = items[position]
        adapterPosition = position
        if (item is GroupDto) {
            if (!item.isMember!!) {
                if (isNetworkActiveWithMessage())
                    viewModel.joinGroup(item)
            } else groupDetails(item)
        }
    }
}