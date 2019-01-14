package com.conversify.ui.main.chats

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.conversify.R
import com.conversify.databinding.FragmentChatsBinding
import com.conversify.ui.base.BaseFragment

class ChatsFragment : BaseFragment() {
    companion object {
        const val TAG = "ChatsFragment"
    }

    private lateinit var binding: FragmentChatsBinding
    private lateinit var viewPagerAdapter: ChatViewPagerAdapter

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_chats


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, getFragmentLayoutResId(), container, false)
        binding.view = this
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPagerAdapter = ChatViewPagerAdapter(requireFragmentManager())
        binding.viewPager.adapter = viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }


}