package com.pulse.ui.search.top

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import com.pulse.R
import com.pulse.data.local.PrefsManager
import com.pulse.data.remote.models.Status
import com.pulse.data.remote.models.loginsignup.ProfileDto
import com.pulse.data.remote.models.people.UserCrossedDto
import com.pulse.data.remote.models.venues.YourVenuesDto
import com.pulse.extensions.handleError
import com.pulse.extensions.isNetworkActive
import com.pulse.extensions.isNetworkActiveWithMessage
import com.pulse.ui.base.BaseFragment
import com.pulse.ui.people.details.PeopleDetailsActivity
import com.pulse.utils.AppConstants
import com.pulse.utils.GlideApp
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
        (rvTopSearch.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        rvTopSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
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

    override fun onClick(position: Int, profile: ProfileDto) {
        val items = adapter.getUpdatedList()
        val item = items[position]
        if (item is ProfileDto) {
            val data = UserCrossedDto()
            data.profile = item
            PrefsManager.get().save(PrefsManager.PREF_PEOPLE_USER_ID, item.id?:"")
            val intent = PeopleDetailsActivity.getStartIntent(requireActivity(), data, AppConstants.REQ_CODE_BLOCK_USER)
            startActivity(intent)
        }
    }
}