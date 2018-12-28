package com.conversify.data.local.models

import android.os.Parcelable
import com.conversify.data.remote.models.loginsignup.InterestDto
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VenueFilters(var categories: List<InterestDto>? = null,
                        var date: Date? = null,
                        var privacy: Privacy? = null,
                        var location: Location? = null) : Parcelable {

    @Parcelize
    data class Date(var dateTimeMillisUtc: Long? = null) : Parcelable

    @Parcelize
    data class Privacy(var publicSelected: Boolean = false,
                       var privateSelected: Boolean = false) : Parcelable

    @Parcelize
    data class Location(
            var name: String? = null,
            var latitude: Double? = null,
            var longitude: Double? = null) : Parcelable
}