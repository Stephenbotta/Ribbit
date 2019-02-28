package com.conversify.ui.conversenearby.post

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.conversify.R
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.custom.LoadingDialog
import com.conversify.utils.GetSampledImage
import com.conversify.utils.MediaPicker
import permissions.dispatcher.RuntimePermissions
import java.io.File

class PostNearByActivity : BaseActivity() {

    companion object {
        private const val EXTRA_FLAG = "EXTRA_FLAG"

        fun getStartIntent(context: Context, flag: Int): Intent {
            return Intent(context, PostNearByActivity::class.java)
                    .putExtra(EXTRA_FLAG, flag)
        }
    }

    private var getSampledImage: GetSampledImage? = null
    private var selectedImage: File? = null
    private lateinit var mediaPicker: MediaPicker
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_near_by)
    }





}