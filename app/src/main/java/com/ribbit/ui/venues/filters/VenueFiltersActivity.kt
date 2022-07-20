package com.ribbit.ui.venues.filters

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.ribbit.R
import com.ribbit.data.MemoryCache
import com.ribbit.data.local.models.VenueFilters
import com.ribbit.ui.base.BaseActivity
import com.ribbit.utils.AppConstants
import kotlinx.android.synthetic.main.activity_venue_filters.*
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class VenueFiltersActivity : BaseActivity(), VenueFiltersAdapter.Callback {
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

    private lateinit var venueFiltersAdapter: VenueFiltersAdapter
    private lateinit var filters: VenueFilters
    override fun onSavedInstance(outState: Bundle?, outPersisent: PersistableBundle?) {
        TODO("Not yet implemented")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venue_filters)

        setListeners()
        setupFilterRecycler()
        setupVenueFilters()
    }

    private fun setListeners() {
        btnClose.setOnClickListener { onBackPressed() }

        btnReset.setOnClickListener {
            showResetFiltersConfirmationDialog()
        }

        btnApply.setOnClickListener {
            setResult(Activity.RESULT_OK, Intent().putExtra(AppConstants.EXTRA_VENUE_FILTERS, filters))
            finish()
        }
    }

    private fun setupFilterRecycler() {
        venueFiltersAdapter = VenueFiltersAdapter(this)
        rvFilterItems.adapter = venueFiltersAdapter
    }

    private fun setupVenueFilters() {
        // Filter will be available in extra if user has already applied once, otherwise null.
        filters = intent.getParcelableExtra<VenueFilters?>(EXTRA_FILTERS) ?: VenueFilters()

        // Form the filter items for each option
        val categories = filters.categories ?: MemoryCache.getInterests()

        val filterDate = filters.date ?: VenueFilters.Date()
        val date = listOf(filterDate)

        val filterPrivacy = filters.privacy ?: VenueFilters.Privacy()
        val privacy = listOf(filterPrivacy)

        val filterLocation = filters.location ?: VenueFilters.Location()
        val location = listOf(filterLocation)

        // Update filter types
        filters.categories = categories
        filters.date = filterDate
        filters.privacy = filterPrivacy
        filters.location = filterLocation

        // Display the filter items for the option that is selected
        rgTypes.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbCategory -> {
                    venueFiltersAdapter.displayItems(categories)
                }

                R.id.rbDate -> {
                    venueFiltersAdapter.displayItems(date)
                }

                R.id.rbPrivacy -> {
                    venueFiltersAdapter.displayItems(privacy)
                }

                R.id.rbLocation -> {
                    venueFiltersAdapter.displayItems(location)
                }
            }
        }

        // Select category by default
        rgTypes.check(rbCategory.id)
    }

    override fun onSelectDateClicked() {
        val date = LocalDate.now()
        val dialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val zonedDateTime = ZonedDateTime.of(year, month + 1, dayOfMonth,
                    0, 0, 0, 0, ZoneId.systemDefault())
            val selectedTimestamp: Long = zonedDateTime.toEpochSecond() * 1000

            // Update the date filter and notify adapter change
            filters.date?.dateTimeMillisUtc = selectedTimestamp
            venueFiltersAdapter.notifyDataSetChanged()
        }, date.year, date.monthValue - 1, date.dayOfMonth)
        dialog.datePicker.minDate = System.currentTimeMillis()
        dialog.show()
    }

    override fun onSelectLocationClicked() {
        /*val builder = PlacePicker.IntentBuilder()
        startActivityForResult(builder.build(this), AppConstants.REQ_CODE_PLACE_PICKER)*/

        val placeFields = listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME)

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, placeFields)
                .build(this)
        startActivityForResult(intent, AppConstants.REQ_CODE_PLACE_PICKER)
    }

    private fun showResetFiltersConfirmationDialog() {
        val dialog = AlertDialog.Builder(this)
                .setMessage(R.string.venue_filters_label_confirm_reset_filter)
                .setPositiveButton(R.string.venue_filters_btn_reset) { _, _ ->
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
        dialog.show()
        val typeface = ResourcesCompat.getFont(this, R.font.roboto_text_regular)
        dialog.findViewById<TextView>(android.R.id.message)?.typeface = typeface
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AppConstants.REQ_CODE_PLACE_PICKER && resultCode == Activity.RESULT_OK && data != null) {
            val place = Autocomplete.getPlaceFromIntent(data)

            // Use place name first if available otherwise use place address
            val locationName = if (!place.name.isNullOrBlank()) {
                place.name.toString()
            } else if (!place.address.isNullOrBlank()) {
                place.address.toString()
            } else {
                ""
            }

            // Update the location filter and notify adapter change
            filters.location?.name = locationName
            filters.location?.latitude = place.latLng?.latitude
            filters.location?.longitude = place.latLng?.longitude
            venueFiltersAdapter.notifyDataSetChanged()
        }
    }
}