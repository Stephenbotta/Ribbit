package com.conversify.ui.newpost

import android.os.Bundle
import com.conversify.R
import com.conversify.extensions.hideKeyboard
import com.conversify.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_new_post.*

class NewPostActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.flContainer, ChooseGroupFragment(), ChooseGroupFragment.TAG)
                    .commit()
        }

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
        }

        btnBack.setOnClickListener {
            it.hideKeyboard()
            onBackPressed()
        }
    }
}