package com.ribbit.ui.search.top

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ribbit.R
import com.ribbit.data.remote.models.Status
import com.ribbit.ui.loginsignup.ProfileDto
import com.ribbit.data.remote.models.people.UserCrossedDto
import com.ribbit.data.remote.models.venues.YourVenuesDto
import com.ribbit.extensions.handleError
import com.ribbit.extensions.isNetworkActive
import com.ribbit.extensions.isNetworkActiveWithMessage
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.people.details.PeopleDetailsActivity
import com.ribbit.utils.AppConstants
import com.ribbit.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_search_top.*


class SearchTopFragment : BaseFragment(), SearchTopAdapter.Callback {
    companion object {
        const val TAG = "SearchTopFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_search_top

    private lateinit var viewModel: SearchTopViewModel
    private lateinit var adapter: SearchTopAdapter
    //    private lateinit var loadingDialog: LoadingDialog
    private var search = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SearchTopViewModel::class.java)
        adapter = SearchTopAdapter(GlideApp.with(this), this)
//        loadingDialog = LoadingDialog(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeChanges()
        setupHomeRecycler()
        getTopSearch()
    }

    private fun observeChanges() {
        viewModel.topSearch.observe(this, Observer { resource ->
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

    }

    private fun getTopSearch(showLoading: Boolean = true) {
        if (isNetworkActiveWithMessage()) {
            viewModel.getTopSearch(showLoading, search)
        }
    }

    private fun setupHomeRecycler() {
        rvTopSearch.adapter = adapter
        (rvTopSearch.itemAnimator as androidx.recyclerview.widget.SimpleItemAnimator).supportsChangeAnimations = false
        rvTopSearch.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1) && viewModel.validForPaging() && isNetworkActive()) {
                    viewModel.getTopSearch(false, search)
                }
            }
        })
    }

    fun search(query: String) {
        this.search = query
        getTopSearch()
    }

    override fun onClick(profile: ProfileDto) {
        val data = UserCrossedDto()
        data.profile = profile
        val intent = PeopleDetailsActivity.getStartIntent(requireActivity(), data,
                AppConstants.REQ_CODE_BLOCK_USER, data.profile?.id ?: "")
        startActivity(intent)
    }
}