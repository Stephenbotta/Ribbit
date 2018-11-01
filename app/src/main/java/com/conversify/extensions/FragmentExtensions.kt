package com.conversify.extensions

import android.support.v4.app.Fragment

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