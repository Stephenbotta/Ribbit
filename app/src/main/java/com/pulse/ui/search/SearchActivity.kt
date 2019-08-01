package com.pulse.ui.search

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.pulse.R
import com.pulse.databinding.ActivitySearchBinding
import com.pulse.extensions.hideKeyboard
import com.pulse.ui.base.BaseActivity
import com.pulse.ui.search.groups.SearchGroupFragment
import com.pulse.ui.search.posts.SearchPostsFragment
import com.pulse.ui.search.tag.SearchTagFragment
import com.pulse.ui.search.top.SearchTopFragment
import com.pulse.ui.search.venues.SearchVenueFragment

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
        listener()
    }

    private fun listener() {
        binding.etCredentials.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(search: CharSequence?, start: Int, before: Int, count: Int) {
                when {
                    binding.viewPagerSearch.currentItem == 0 -> (viewPagerAdapter.fragments[0] as SearchTopFragment).search(search.toString())
                    binding.viewPagerSearch.currentItem == 1 -> (viewPagerAdapter.fragments[1] as SearchTagFragment).search(search.toString())
                    binding.viewPagerSearch.currentItem == 2 -> (viewPagerAdapter.fragments[2] as SearchPostsFragment).search(search.toString())
                    binding.viewPagerSearch.currentItem == 3 -> (viewPagerAdapter.fragments[3] as SearchGroupFragment).search(search.toString())
//                    binding.viewPagerSearch.currentItem == 4 -> (viewPagerAdapter.fragments[4] as SearchVenueFragment).search(search.toString())
                }
            }
        })

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
