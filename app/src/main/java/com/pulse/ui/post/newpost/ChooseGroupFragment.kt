package com.pulse.ui.post.newpost

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.pulse.R
import com.pulse.data.remote.models.Status
import com.pulse.data.remote.models.groups.GroupDto
import com.pulse.extensions.handleError
import com.pulse.extensions.isNetworkActiveWithMessage
import com.pulse.ui.base.BaseFragment
import com.pulse.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_choose_group.*

class ChooseGroupFragment : BaseFragment() {
    companion object {
        const val TAG = "NewPostChooseGroupFragment"
    }

    private lateinit var viewModel: NewPostViewModel
    private lateinit var adapter: ChooseGroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProviders.of(this)[NewPostViewModel::class.java]
        adapter = ChooseGroupAdapter(GlideApp.with(this)) { group ->
            navigateToNewPostFragment(group)
        }
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_choose_group

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_choose_group, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvChooseGroup.adapter = adapter
        observeChanges()
        getGroups()
    }

    private fun observeChanges() {
        viewModel.yourGroups.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    val groups = resource.data ?: emptyList()
                    adapter.setLoading(false)
                    adapter.displayGroups(groups)
                }

                Status.ERROR -> {
                    handleError(resource.error)
                    adapter.setLoading(false)
                }

                Status.LOADING -> {
                    adapter.setLoading(true)
                }
            }
        })
    }

    private fun getGroups() {
        if (isNetworkActiveWithMessage()) {
            viewModel.getYourGroups()
        }
    }

    private fun navigateToNewPostFragment(group: GroupDto? = null) {
        (activity as NewPostActivity).changeBackButtonText(getString(R.string.back))
        val fragment = NewPostFragment.newInstance(group)
        fragmentManager?.apply {
            beginTransaction()
                    .setCustomAnimations(R.anim.parallax_right_in, R.anim.parallax_left_out,
                            R.anim.parallax_left_in, R.anim.parallax_right_out)
                    .replace(R.id.flContainer, fragment, NewPostFragment.TAG)
                    .addToBackStack(null)
                    .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.menuSkip) {
            navigateToNewPostFragment()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}