package com.checkIt.extensions

import androidx.fragment.app.Fragment
import com.checkIt.data.local.models.AppError

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