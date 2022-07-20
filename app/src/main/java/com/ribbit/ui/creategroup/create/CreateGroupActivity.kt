package com.ribbit.ui.creategroup.create

import android.os.Bundle
import android.os.PersistableBundle
import com.ribbit.R
import com.ribbit.extensions.hideKeyboard
import com.ribbit.ui.base.BaseActivity
import com.ribbit.ui.creategroup.categories.GroupCategoriesFragment
import kotlinx.android.synthetic.main.activity_create_venue.*

class CreateGroupActivity : BaseActivity() {
    override fun onSavedInstance(outState: Bundle?, outPersisent: PersistableBundle?) {
        TODO("Not yet implemented")
    }

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

        btnBack.setOnClickListener {
            it.hideKeyboard()
            onBackPressed()
        }
    }
}