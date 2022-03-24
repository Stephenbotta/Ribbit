package com.ribbit.ui.main.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import com.ribbit.R
import com.ribbit.databinding.FragmentChatsBinding
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.main.chats.group.GroupChatFragment
import com.ribbit.ui.main.chats.individual.IndividualChatFragment

class ChatsFragment : BaseFragment() {
    companion object {
        const val TAG = "ChatsFragment"
    }

    private lateinit var binding: FragmentChatsBinding
    private lateinit var viewPagerAdapter: ChatViewPagerAdapter

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_chats

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, getFragmentLayoutResId(), container, false)
        binding.lifecycleOwner = this
        binding.view = this
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPagerAdapter = ChatViewPagerAdapter(requireContext(), requireFragmentManager())
        viewPagerAdapter.addFragments(IndividualChatFragment())
        viewPagerAdapter.addFragments(GroupChatFragment())
        binding.viewPager.adapter = viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        inItClasses()
    }

    private fun inItClasses() {
        binding.searchChatView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String?): Boolean {
                if (binding.viewPager.currentItem == 0) {
                    val fragment = (viewPagerAdapter.fragments[0] as IndividualChatFragment)
                    if (fragment.isVisible) {
                        fragment.search(query ?: "")
                    }
                } else if (binding.viewPager.currentItem == 1) {
                    val fragment = (viewPagerAdapter.fragments[1] as GroupChatFragment)
                    if (fragment.isVisible) {
                        fragment.search(query ?: "")
                    }
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        })

    }

}