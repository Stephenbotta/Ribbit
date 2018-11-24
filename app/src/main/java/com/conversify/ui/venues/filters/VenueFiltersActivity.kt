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
import com.google.android.gms.location.places.Place
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

    private lateinit var filtersCategoryAdapter: FiltersCategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venue_filters)

        btnClose.setOnClickListener { onBackPressed() }

        btnReset.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }

        filtersCategoryAdapter = FiltersCategoryAdapter(this)
        rvFilterItems.adapter = filtersCategoryAdapter

        val filters = intent.getParcelableExtra<VenueFilters?>(EXTRA_FILTERS)
        val categories = MemoryCache.getInterests()
        val date = filters?.date ?: VenueFilters.Date()
        val privacy = filters?.privacy ?: VenueFilters.Privacy()
        val location = filters?.location ?: VenueFilters.Location("", 0.0, 0.0)

        rgTypes.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbCategory -> {
                    filtersCategoryAdapter.displayItems(categories)
                }

                R.id.rbDate -> {
                    filtersCategoryAdapter.displayItems(listOf(date))
                }

                R.id.rbPrivacy -> {
                    filtersCategoryAdapter.displayItems(listOf(privacy))
                }

                R.id.rbLocation -> {
                    filtersCategoryAdapter.displayItems(listOf(location))
                }
            }
        }

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
        setResult(Activity.RESULT_OK,
                Intent().putExtra(AppConstants.EXTRA_VENUE_FILTERS, VenueFilters(category = category)))
        finish()
    }

    override fun onSelectDateClicked() {
        val date = LocalDate.now()
        val dialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val zonedDateTime = ZonedDateTime.of(year, month + 1, dayOfMonth,
                    0, 0, 0, 0, ZoneId.systemDefault())
            val selectedTimestamp: Long = zonedDateTime.toEpochSecond() * 1000
            val filters = VenueFilters(date = VenueFilters.Date(selectedTimestamp))
            setResult(Activity.RESULT_OK,
                    Intent().putExtra(AppConstants.EXTRA_VENUE_FILTERS, filters))
            finish()
        }, date.year, date.monthValue - 1, date.dayOfMonth)
        dialog.datePicker.minDate = System.currentTimeMillis()
        dialog.show()
    }

    override fun onPrivacySelected(isPrivate: Boolean) {
        val filters = VenueFilters(privacy = VenueFilters.Privacy(isPrivate))
        setResult(Activity.RESULT_OK,
                Intent().putExtra(AppConstants.EXTRA_VENUE_FILTERS, filters))
        finish()
    }

    override fun onSelectLocationClicked() {
        val builder = PlacePicker.IntentBuilder()
        startActivityForResult(builder.build(this), AppConstants.REQ_CODE_PLACE_PICKER)
    }

    private fun getLocationFilter(place: Place): VenueFilters.Location {
        val name = if (!place.name.isNullOrBlank()) {
            place.name.toString()
        } else if (!place.address.isNullOrBlank()) {
            place.address.toString()
        } else {
            ""
        }

        return VenueFilters.Location(name = name,
                latitude = place.latLng.latitude,
                longitude = place.latLng.longitude)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AppConstants.REQ_CODE_PLACE_PICKER && resultCode == Activity.RESULT_OK) {
            val filter = getLocationFilter(PlacePicker.getPlace(this, data))
            setResult(Activity.RESULT_OK,
                    Intent().putExtra(AppConstants.EXTRA_VENUE_FILTERS, VenueFilters(location = filter)))
            finish()
        }
    }
}