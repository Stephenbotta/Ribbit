package com.conversify.ui.conversenearby.post

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.post.CreatePostRequest
import com.conversify.extensions.shortToast
import com.conversify.ui.base.BaseActivity
import com.conversify.utils.AppConstants
import com.conversify.utils.AppUtils
import com.conversify.utils.DateTimePicker
import com.conversify.utils.DateTimeUtils
import com.google.android.gms.location.places.ui.PlacePicker
import kotlinx.android.synthetic.main.activity_submit_post.*


class SubmitPostActivity : BaseActivity(), View.OnClickListener {

    companion object {
        private const val EXTRA_FLAG = "EXTRA_FLAG"

        fun getStartIntent(context: Context, flag: Int): Intent {
            return Intent(context, SubmitPostActivity::class.java)
                    .putExtra(EXTRA_FLAG, flag)
        }
    }

    private var flag = 0
    private val request = CreatePostRequest()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_post)

        flag = intent.getIntExtra(EXTRA_FLAG, 0)
        setData()
        setListener()
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
                val data = Intent()
                data.putExtra(AppConstants.EXTRA_POST_DATA, request)
                setResult(Activity.RESULT_OK, data)
                finish()
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
