package com.conversify.ui.venues.filters

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.conversify.R
import com.conversify.data.MemoryCache
import com.conversify.data.local.models.VenueFilters
import com.conversify.data.remote.models.loginsignup.InterestDto
import com.conversify.ui.base.BaseActivity
import com.conversify.utils.AppConstants
import com.google.android.gms.location.places.ui.PlacePicker
import kotlinx.android.synthetic.main.activity_venue_filters.*
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class VenueFiltersActivity : BaseActivity(), FiltersCategoryAdapter.Callback {
    companion object {
        private const val EXTRA_FILTERS = "EXTRA_VENUE_FILTERS"

        fun getStartIntent(context: Context, filters: VenueFilters?): Intent {
            val intent = Intent(context, VenueFiltersActivity::class.java)
            if (filters != null) {
                intent.putExtra(EXTRA_FILTERS, filters)
            }
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venue_filters)

        btnClose.setOnClickListener { onBackPressed() }

        btnReset.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }

        val filtersCategoryAdapter = FiltersCategoryAdapter(this)
        rvFilterItems.adapter = filtersCategoryAdapter

        // Filter will be available in extra if user has already applied once, otherwise null.
        val filters = intent.getParcelableExtra<VenueFilters?>(EXTRA_FILTERS)

        // Form the filter items for each option
        val categories = MemoryCache.getInterests()
        val date = listOf(filters?.date ?: VenueFilters.Date())
        val privacy = listOf(filters?.privacy ?: VenueFilters.Privacy())
        val location = listOf(filters?.location ?: VenueFilters.Location("", 0.0, 0.0))

        // Display the filter items for the option that is selected
        rgTypes.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbCategory -> {
                    filtersCategoryAdapter.displayItems(categories)
                }

                R.id.rbDate -> {
                    filtersCategoryAdapter.displayItems(date)
                }

                R.id.rbPrivacy -> {
                    filtersCategoryAdapter.displayItems(privacy)
                }

                R.id.rbLocation -> {
                    filtersCategoryAdapter.displayItems(location)
                }
            }
        }

        // Check the radio button whose value is available in the filter
        rgTypes.check(when {
            filters?.date != null -> {
                rbDate.id
            }

            filters?.privacy != null -> {
                rbPrivacy.id
            }

            filters?.location != null -> {
                rbLocation.id
            }

            else -> {
                rbCategory.id
            }
        })
    }

    override fun onVenueCategoryClicked(category: InterestDto) {
        // Send the result with only category filter
        val filters = VenueFilters(category = category)
        sendFiltersResult(filters)
    }

    override fun onSelectDateClicked() {
        val date = LocalDate.now()
        val dialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val zonedDateTime = ZonedDateTime.of(year, month + 1, dayOfMonth,
                    0, 0, 0, 0, ZoneId.systemDefault())
            val selectedTimestamp: Long = zonedDateTime.toEpochSecond() * 1000

            // Send the result with only date filter
            val filters = VenueFilters(date = VenueFilters.Date(selectedTimestamp))
            sendFiltersResult(filters)
        }, date.year, date.monthValue - 1, date.dayOfMonth)
        dialog.datePicker.minDate = System.currentTimeMillis()
        dialog.show()
    }

    override fun onPrivacySelected(isPrivate: Boolean) {
        // Send the result with only privacy filter
        val filters = VenueFilters(privacy = VenueFilters.Privacy(isPrivate))
        sendFiltersResult(filters)
    }

    override fun onSelectLocationClicked() {
        val builder = PlacePicker.IntentBuilder()
        startActivityForResult(builder.build(this), AppConstants.REQ_CODE_PLACE_PICKER)
    }

    private fun sendFiltersResult(filters: VenueFilters) {
        setResult(Activity.RESULT_OK, Intent().putExtra(AppConstants.EXTRA_VENUE_FILTERS, filters))
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AppConstants.REQ_CODE_PLACE_PICKER && resultCode == Activity.RESULT_OK) {
            val place = PlacePicker.getPlace(this, data)

            // Use place name first if available otherwise use place address
            val locationName = if (!place.name.isNullOrBlank()) {
                place.name.toString()
            } else if (!place.address.isNullOrBlank()) {
                place.address.toString()
            } else {
                ""
            }
            val locationFilter = VenueFilters.Location(name = locationName,
                    latitude = place.latLng.latitude,
                    longitude = place.latLng.longitude)

            // Send the result with only location filter
            val filters = VenueFilters(location = locationFilter)
            sendFiltersResult(filters)
        }
    }
}