package com.conversify.ui.post.newpost

import android.os.Bundle
import com.conversify.R
import com.conversify.data.local.UserManager
import com.conversify.extensions.hideKeyboard
import com.conversify.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_new_post.*

class NewPostActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
        }

        btnBack.setOnClickListener {
            it.hideKeyboard()
            onBackPressed()
        }

        if (savedInstanceState == null) {
            // If group count is 0, then directly show new post fragment otherwise show choose group fragment.
            if (UserManager.getGroupCount() == 0) {
                supportFragmentManager.beginTransaction()
                        .add(R.id.flContainer, NewPostFragment(), NewPostFragment.TAG)
                        .commit()
            } else {
                supportFragmentManager.beginTransaction()
                        .add(R.id.flContainer, ChooseGroupFragment(), ChooseGroupFragment.TAG)
                        .commit()
            }
        }
    }
}