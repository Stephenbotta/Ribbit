package com.ribbit.extensions

import com.google.android.gms.maps.model.LatLng

fun List<Double>?.toLatLng(): LatLng {
    return if (this == null) {
        LatLng(0.0, 0.0)
    } else {
        LatLng(getOrNull(1) ?: 0.0, getOrNull(0) ?: 0.0)
    }
}