package com.pulse.ui.createvenue

import android.os.Bundle
import com.pulse.R
import com.pulse.extensions.hideKeyboard
import com.pulse.ui.base.BaseActivity
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