package com.checkIt.ui.search.groups

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.checkIt.R
import com.checkIt.data.remote.models.Status
import com.checkIt.data.remote.models.groups.GroupDto
import com.checkIt.data.remote.models.venues.YourVenuesDto
import com.checkIt.extensions.handleError
import com.checkIt.extensions.isNetworkActive
import com.checkIt.extensions.isNetworkActiveWithMessage
import com.checkIt.extensions.longToast
import com.checkIt.ui.base.BaseFragment
import com.checkIt.ui.custom.LoadingDialog
import com.checkIt.ui.groups.groupposts.GroupPostsActivity
import com.checkIt.utils.AppConstants
import com.checkIt.utils.GlideApp
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
                            items[adapterPosition ?: 0] = group ?: GroupDto()
                            groupDetails(group ?: GroupDto())
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQ_CODE_EXIT_GROUP -> {
                if (resultCode == Activity.RESULT_OK) {
                    val group = data?.getParcelableExtra<GroupDto>(AppConstants.EXTRA_GROUP)
                    if (group != null && group.isMember == false) {
                        getGroupSearch()
                    }
                }
            }
        }
    }

    private fun groupDetails(group: GroupDto) {
        val intent = GroupPostsActivity.start(requireActivity(), group)
        startActivityForResult(intent, AppConstants.REQ_CODE_EXIT_GROUP)
        adapter.notifyDataSetChanged()
    }

    private fun getGroupSearch(showLoading: Boolean = true) {
        if (isNetworkActiveWithMessage()) {
            viewModel.getGroupSearch(showLoading, search)
        }
    }

    private fun setupHomeRecycler() {
        rvGroupSearch.adapter = adapter
        (rvGroupSearch.itemAnimator as androidx.recyclerview.widget.SimpleItemAnimator).supportsChangeAnimations = false
        rvGroupSearch.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
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
            if (item.isMember == false) {
                if (isNetworkActiveWithMessage())
                    viewModel.joinGroup(item)
            } else groupDetails(item)
        }
    }
}