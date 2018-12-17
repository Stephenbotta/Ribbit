package com.conversify.ui.creategroup.create

import android.os.Bundle
import com.conversify.R
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.creategroup.categories.GroupCategoriesFragment
import kotlinx.android.synthetic.main.activity_create_venue.*

class CreateGroupActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.flContainer, GroupCategoriesFragment(), GroupCategoriesFragment.TAG)
                    .commit()
        }

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
        }

        btnBack.setOnClickListener { onBackPressed() }
    }
}