package com.conversify.ui.search

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.databinding.ActivitySearchBinding
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.search.groups.SearchGroupFragment
import com.conversify.ui.search.posts.SearchPostsFragment
import com.conversify.ui.search.tag.SearchTagFragment
import com.conversify.ui.search.top.SearchTopFragment
import com.conversify.ui.search.venues.SearchVenueFragment

class SearchActivity : BaseActivity() {

    companion object {
        private const val EXTRA_FLAG = "EXTRA_FLAG"

        fun getStartIntent(context: Context, flag: Int): Intent {
            return Intent(context, SearchActivity::class.java)
                    .putExtra(EXTRA_FLAG, flag)
        }
    }

    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModel: SearchViewModel
    private lateinit var viewPagerAdapter: SearchPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)
        binding.setLifecycleOwner(this)
        binding.view = this
        inItClasses()
    }

    private fun inItClasses() {
        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        binding.viewModel = viewModel

        viewPagerAdapter = SearchPagerAdapter(this, supportFragmentManager)
        viewPagerAdapter.addFragments(SearchTopFragment())
        viewPagerAdapter.addFragments(SearchTagFragment())
        viewPagerAdapter.addFragments(SearchPostsFragment())
        viewPagerAdapter.addFragments(SearchGroupFragment())
        viewPagerAdapter.addFragments(SearchVenueFragment())
        binding.viewPagerSearch.adapter = viewPagerAdapter
        binding.tabLayoutSearch.setupWithViewPager(binding.viewPagerSearch)

    }

    fun onClick(v: View) {

        when (v.id) {

            R.id.ivBack -> onBackPressed()

        }
    }

}
