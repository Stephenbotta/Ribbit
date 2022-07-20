package com.ribbit.ui.conversenearby.post

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import com.ribbit.R
import com.ribbit.ui.base.BaseActivity
import com.ribbit.utils.FragmentSwitcher

class PostNearByActivity : BaseActivity() {

    companion object {
        private const val EXTRA_FLAG = "EXTRA_FLAG"

        fun getStartIntent(context: Context, flag: Int): Intent {
            return Intent(context, PostNearByActivity::class.java)
                    .putExtra(EXTRA_FLAG, flag)
        }
    }

    private var flag = 0
    private lateinit var fragmentSwitcher: FragmentSwitcher
    override fun onSavedInstance(outState: Bundle?, outPersisent: PersistableBundle?) {
        TODO("Not yet implemented")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        flag = intent.getIntExtra(EXTRA_FLAG, 0)
        fragmentSwitcher = FragmentSwitcher(supportFragmentManager, R.id.flProfileContainer)
        fragmentSwitcher.addFragment(PostNearByFragment(), PostNearByFragment.TAG)

    }

    fun getFlag(): Int = flag

}