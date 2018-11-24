package com.conversify.data.local.models

import android.os.Parcelable
import com.conversify.data.remote.models.loginsignup.InterestDto
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VenueFilters(val category: InterestDto? = null,
                        val date: Date? = null,
                        val privacy: Privacy? = null,
                        val location: Location? = null) : Parcelable {

    @Parcelize
    data class Date(val dateTimeMillisUtc: Long? = null) : Parcelable

    @Parcelize
    data class Privacy(val isPrivate: Boolean? = null) : Parcelable

    @Parcelize
    data class Location(
            var name: String,
            var latitude: Double,
            var longitude: Double) : Parcelable
}