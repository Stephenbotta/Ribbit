package com.conversify.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.conversify.data.remote.PushType
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.landing.LandingActivity
import com.conversify.ui.main.MainActivity
import com.conversify.utils.AppConstants

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val type = intent.getStringExtra("TYPE")
        val intent = Intent(this, MainActivity::class.java)
        if (!type.isNullOrEmpty()) {
            intent.putExtra("TYPE", type)
            intent.putExtra("id", this.intent.getStringExtra("id"))
            when (type) {
                PushType.CHAT -> {
                    val data = this.intent.getStringExtra("senderDetails")
                    intent.putExtra("data", data)
                }
                PushType.GROUP_CHAT -> {
                    val data = this.intent.getStringExtra("groupDetails")
                    intent.putExtra("data", data)
                }
                PushType.VENUE_CHAT -> {
                    val data = this.intent.getStringExtra("groupDetails")
                    intent.putExtra("data", data)
                }
                else -> {

                }
            }
            startActivity(intent)
            finishAffinity()
        } else {
            Handler().postDelayed({
                startActivity(Intent(this, LandingActivity::class.java))
                finish()
            }, AppConstants.SPLASH_TIMEOUT)
        }
    }
}