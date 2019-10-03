package com.ribbit.ui.conversenearby

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.ribbit.R
import com.ribbit.ui.base.BaseActivity
import com.ribbit.ui.conversenearby.post.PostNearByActivity
import com.ribbit.utils.AppConstants
import kotlinx.android.synthetic.main.activity_select_near_by.*

class SelectNearByActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_near_by)
        setListener()
    }

    private fun setListener() {
        btnBack.setOnClickListener { onBackPressed() }
        someoneNearBy.setOnClickListener { start(AppConstants.REQ_CODE_CONVERSE_NEARBY) }
        crossedPath.setOnClickListener { start(AppConstants.REQ_CODE_CROSSED_PATH) }
    }

    private fun start(flag: Int) {
        val intent = PostNearByActivity.getStartIntent(this, flag)
        startActivityForResult(intent, AppConstants.REQ_CODE_CREATE_NEW_POST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            AppConstants.REQ_CODE_CREATE_NEW_POST -> {
                if (resultCode == Activity.RESULT_OK)
                    finish()
            }

        }
    }
}
