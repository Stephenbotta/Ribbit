package com.conversify.ui.createvenue

import android.os.Bundle
import com.conversify.R
import com.conversify.ui.base.BaseActivity

class CreateVenueActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_venue)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.flContainer, VenueCategoriesFragment(), VenueCategoriesFragment.TAG)
                    .commit()
        }
    }
}