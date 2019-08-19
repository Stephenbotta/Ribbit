package com.checkIt.ui.search

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.checkIt.R
import com.checkIt.databinding.ActivitySearchBinding
import com.checkIt.extensions.hideKeyboard
import com.checkIt.ui.base.BaseActivity
import com.checkIt.ui.search.groups.SearchGroupFragment
import com.checkIt.ui.search.posts.SearchPostsFragment
import com.checkIt.ui.search.tag.SearchTagFragment
import com.checkIt.ui.search.top.SearchTopFragment

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
        binding.lifecycleOwner = this
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
//        viewPagerAdapter.addFragments(SearchVenueFragment())
        binding.viewPagerSearch.adapter = viewPagerAdapter
        binding.tabLayoutSearch.tabGravity = TabLayout.GRAVITY_CENTER
        binding.tabLayoutSearch.tabMode = TabLayout.MODE_SCROLLABLE
        binding.tabLayoutSearch.setupWithViewPager(binding.viewPagerSearch)
        binding.viewPagerSearch.offscreenPageLimit = 4
        listener()
    }

    private fun listener() {
        binding.etCredentials.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(search: CharSequence?, start: Int, before: Int, count: Int) {
                val fragment = viewPagerAdapter.getItem(binding.viewPagerSearch.currentItem)
                val query = search.toString().trim()
                searchInFragment(fragment, query)
            }
        })

        binding.tabLayoutSearch.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {}

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabSelected(tab: TabLayout.Tab) {
                val fragment = viewPagerAdapter.getItem(tab.position)
                val query = binding.etCredentials.text.toString().trim()
                searchInFragment(fragment, query)
            }
        })
    }

    private fun searchInFragment(fragment: Fragment, query: String) {
        when (fragment) {
            is SearchTopFragment -> {
                fragment.search(query)
            }
            is SearchTagFragment -> {
                fragment.search(query)
            }
            is SearchPostsFragment -> {
                fragment.search(query)
            }
            is SearchGroupFragment -> {
                fragment.search(query)
            }
        }
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.ivBack -> onBackPressed()

            R.id.tvCancel -> {
                binding.etCredentials.setText("")
                binding.tvCancel.hideKeyboard()
            }
        }
    }
}
