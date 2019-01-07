package com.conversify.ui.people.details

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.ui.base.BaseActivity

class PeopleDetailsActivity : BaseActivity() {

    private lateinit var viewModel: PeopleDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people_details)
        inItClasses()
    }

    private fun inItClasses() {
        viewModel = ViewModelProviders.of(this).get(PeopleDetailsViewModel::class.java)


    }

    fun onClick(v: View) {

        when (v.id) {


        }

    }
}