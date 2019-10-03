package com.ribbit.ui.search.tag

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ribbit.R
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.extensions.handleError
import com.ribbit.extensions.isNetworkActive
import com.ribbit.extensions.isNetworkActiveWithMessage
import com.ribbit.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_search_tags.*

class SearchTagFragment : BaseFragment(), SearchTagAdapter.Callback {

    companion object {
        const val TAG = "SearchTagFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_search_tags

    private lateinit var viewModel: SearchTagViewModel
    private lateinit var adapter: SearchTagAdapter
    //    private lateinit var loadingDialog: LoadingDialog
    private var search = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SearchTagViewModel::class.java)
        adapter = SearchTagAdapter(this)
//        loadingDialog = LoadingDialog(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeChanges()
        setupHomeRecycler()
        getTagSearch()
    }

    private fun observeChanges() {
        viewModel.tagSearch.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    val data = resource.data?.result ?: emptyList()
                    val firstPage = resource.data?.isFirstPage ?: true
                    if (firstPage) {
                        adapter.displayItems(data)
                    } else {
                        adapter.addMoreItems(data)
                    }
                }

                Status.ERROR -> {
                    handleError(resource.error)
                }

                Status.LOADING -> {
                }
            }
        })
    }

    private fun getTagSearch(showLoading: Boolean = true) {
        if (isNetworkActiveWithMessage()) {
            viewModel.getTagSearch(showLoading, search)
        }
    }

    private fun setupHomeRecycler() {
        rvTagSearch.adapter = adapter
        (rvTagSearch.itemAnimator as androidx.recyclerview.widget.SimpleItemAnimator).supportsChangeAnimations = false
        rvTagSearch.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1) && viewModel.validForPaging() && isNetworkActive()) {
                    viewModel.getTagSearch(false, search)
                }
            }
        })
    }

    fun search(query: String) {
        this.search = query
        getTagSearch()
    }

    override fun onClick(profile: ProfileDto) {
        if (requireContext().isNetworkActiveWithMessage())
            viewModel.postFollowUnFollowTag(profile.id ?: "", profile.isFollowing ?: false)
    }
}