package com.pulse.extensions

import android.support.v4.app.Fragment
import com.pulse.data.local.models.AppError

fun Fragment.isNetworkActive(): Boolean {
    val activity = activity
    activity ?: return false
    return activity.isNetworkActive()
}

fun Fragment.isNetworkActiveWithMessage(): Boolean {
    val activity = activity
    activity ?: return false
    return activity.isNetworkActiveWithMessage()
}

fun Fragment.handleError(error: AppError?) {
    activity?.handleError(error)
}