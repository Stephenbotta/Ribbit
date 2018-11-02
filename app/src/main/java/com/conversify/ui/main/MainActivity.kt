package com.conversify.ui.main

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.conversify.R
import com.conversify.data.local.UserManager
import com.conversify.ui.landing.LandingActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLogout.setOnClickListener {
            UserManager.removeProfile()
            startActivity(Intent(this, LandingActivity::class.java))
            finishAffinity()
        }
    }
}