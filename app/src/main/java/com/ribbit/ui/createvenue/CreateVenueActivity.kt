package com.ribbit.ui.createvenue

import android.os.Bundle
import com.ribbit.R
import com.ribbit.extensions.hideKeyboard
import com.ribbit.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_create_venue.*

class CreateVenueActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_venue)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.flContainer, VenueCategoriesFragment(), VenueCategoriesFragment.TAG)
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