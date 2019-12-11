package com.ribbit.extensions

import androidx.navigation.NavOptions
import com.google.android.gms.maps.model.LatLng
import com.ribbit.R

fun List<Double>?.toLatLng(): LatLng {
    return if (this == null) {
        LatLng(0.0, 0.0)
    } else {
        LatLng(getOrNull(1) ?: 0.0, getOrNull(0) ?: 0.0)
    }
}

fun getAnimNavOptions(): NavOptions {
    val navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)  // Used to prevent multiple copies of the same destination
            .setEnterAnim(R.anim.slide_in_from_right)
            .setExitAnim(R.anim.slide_out_to_left)
            .setPopEnterAnim(R.anim.slide_in_from_left)
            .setPopExitAnim(R.anim.slide_out_to_right)
            .build()
    return navOptions
}