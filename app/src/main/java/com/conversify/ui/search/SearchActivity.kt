package com.conversify.ui.search

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.conversify.R
import com.conversify.databinding.ActivitySearchBinding
import com.conversify.extensions.hideKeyboard
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.search.groups.SearchGroupFragment
import com.conversify.ui.search.posts.SearchPostsFragment
import com.conversify.ui.search.tag.SearchTagFragment
import com.conversify.ui.search.top.SearchTopFragment
import com.conversify.ui.search.venues.SearchVenueFragment
import kotlinx.android.synthetic.main.activity_search.*

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
        binding.tabLayoutSearch.tabGravity = TabLayout.GRAVITY_CENTER
        binding.tabLayoutSearch.tabMode = TabLayout.MODE_SCROLLABLE
        binding.tabLayoutSearch.setupWithViewPager(binding.viewPagerSearch)
        listener()
    }

    private fun listener() {
        binding.etCredentials.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(search: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.viewPagerSearch.currentItem == 0) {
                    (viewPagerAdapter.fragments[0] as SearchTopFragment).search(search.toString())
                } else if (binding.viewPagerSearch.currentItem == 1) {
                    (viewPagerAdapter.fragments[1] as SearchTagFragment).search(search.toString())
                } else if (binding.viewPagerSearch.currentItem == 2) {
                    (viewPagerAdapter.fragments[2] as SearchPostsFragment).search(search.toString())
                } else if (binding.viewPagerSearch.currentItem == 3) {
                    (viewPagerAdapter.fragments[3] as SearchGroupFragment).search(search.toString())
                } else if (binding.viewPagerSearch.currentItem == 4) {
                    (viewPagerAdapter.fragments[4] as SearchVenueFragment).search(search.toString())
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
