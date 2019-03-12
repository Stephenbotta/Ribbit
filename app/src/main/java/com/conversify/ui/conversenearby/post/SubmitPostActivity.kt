package com.conversify.ui.conversenearby.post

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.data.local.models.AppError
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.post.CreatePostRequest
import com.conversify.extensions.handleError
import com.conversify.extensions.shortToast
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.custom.LoadingDialog
import com.conversify.utils.AppConstants
import com.conversify.utils.AppUtils
import com.conversify.utils.DateTimePicker
import com.conversify.utils.DateTimeUtils
import com.google.android.gms.location.places.ui.PlacePicker
import kotlinx.android.synthetic.main.activity_submit_post.*
import java.io.File


class SubmitPostActivity : BaseActivity(), View.OnClickListener {

    companion object {
        private const val EXTRA_FLAG = "EXTRA_FLAG"
        private const val EXTRA_REQUEST_DATA = "EXTRA_REQUEST_DATA"
        private const val EXTRA_FILE = "EXTRA_FILE"

        fun getStartIntent(context: Context, flag: Int, request: CreatePostRequest, selectedImagePath: String?): Intent {
            return Intent(context, SubmitPostActivity::class.java)
                    .putExtra(EXTRA_FLAG, flag)
                    .putExtra(EXTRA_REQUEST_DATA, request)
                    .putExtra(EXTRA_FILE, selectedImagePath)
        }
    }

    private lateinit var loadingDialog: LoadingDialog
    private var flag = 0
    private val request: CreatePostRequest by lazy { intent.getParcelableExtra<CreatePostRequest>(EXTRA_REQUEST_DATA) }
    private val selectedImagePath: String by lazy { intent.getStringExtra(EXTRA_FILE) ?: "" }
    private val viewModel by lazy { ViewModelProviders.of(this)[SubmitPostViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_post)
        loadingDialog = LoadingDialog(this)
        flag = intent.getIntExtra(EXTRA_FLAG, 0)
        setData()
        setListener()
        observeChanges()
    }

    private fun setData() {
        when (flag) {
            AppConstants.REQ_CODE_CONVERSE_NEARBY -> {
            }
            AppConstants.REQ_CODE_CROSSED_PATH -> {
                btnNext.isEnabled = false
            }
        }
    }

    private fun setListener() {
        btnBack.setOnClickListener(this)
        btnNext.setOnClickListener(this)
        tvSelectLocation.setOnClickListener(this)
        tvLabelStartDateAndTime.setOnClickListener(this)
        tvStartDateAndTime.setOnClickListener(this)
        tvStartChange.setOnClickListener(this)
        tvLabelEndDateAndTime.setOnClickListener(this)
        tvEndDateAndTime.setOnClickListener(this)
        tvEndChange.setOnClickListener(this)
    }

    private fun observeChanges() {
        viewModel.createPost.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    shortToast(getString(R.string.post_upload_successfully))
                    loadingDialog.setLoading(false)
                    setResult(Activity.RESULT_OK)
                    finish()
                }

                Status.ERROR -> {
                    loadingDialog.setLoading(false)
                    if (resource.error != AppError.WaitingForNetwork) {
                        handleError(resource.error)
                    }
                }

                Status.LOADING -> {
                    loadingDialog.setLoading(true)
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btnBack -> {
                onBackPressed()
            }

            R.id.btnNext -> {
                if (request.meetingTime != null)
                    if (!checkDateTime()) {
                        shortToast("Expiration time should be greater than meeting time")
                        return
                    }
//                val data = Intent()
//                data.putExtra(AppConstants.EXTRA_POST_DATA, request)
//                setResult(Activity.RESULT_OK, data)
//                finish()

                if (selectedImagePath.isNotBlank()) {
                    viewModel.createPost(request, File(selectedImagePath))
                } else {
                    viewModel.createPost(request, null)
                }
            }

            R.id.tvSelectLocation -> {
                val builder = PlacePicker.IntentBuilder()
                startActivityForResult(builder.build(this), AppConstants.REQ_CODE_PLACE_PICKER)
            }

            R.id.tvLabelStartDateAndTime, R.id.tvStartDateAndTime, R.id.tvStartChange -> {
                DateTimePicker(this,
                        minDateMillis = System.currentTimeMillis()) { selectedDateTime ->
                    tvStartDateAndTime.text = DateTimeUtils.formatVenueDateTime(selectedDateTime)
                    request.meetingTime = selectedDateTime.toInstant().toEpochMilli()
                }.show()
            }

            R.id.tvLabelEndDateAndTime, R.id.tvEndDateAndTime, R.id.tvEndChange -> {
                DateTimePicker(this,
                        minDateMillis = System.currentTimeMillis()) { selectedDateTime ->
                    tvEndDateAndTime.text = DateTimeUtils.formatVenueDateTime(selectedDateTime)
                    request.expirationTime = selectedDateTime.toInstant().toEpochMilli()
                }.show()
            }

        }
    }

    private fun checkDateTime(): Boolean {
        val startDate = request.meetingTime ?: 0
        val endDate = request.expirationTime ?: 0
        return startDate < endDate
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQ_CODE_PLACE_PICKER -> {
                if (resultCode == Activity.RESULT_OK) {
                    val place = PlacePicker.getPlace(this, data)
                    request.locationLat = place.latLng.latitude
                    request.locationLong = place.latLng.longitude
                    request.locationName = place.name?.toString()
                    request.locationAddress = place.address?.toString()
                    tvSelectLocation.text = AppUtils.getFormattedAddress(request.locationName, request.locationAddress)
                    btnNext.isEnabled = true
                }
            }

        }
    }
}
