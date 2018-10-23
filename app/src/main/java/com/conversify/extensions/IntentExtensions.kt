package com.conversify.extensions

import android.content.Intent

fun Intent.isActivityLaunchedFromHistory(): Boolean {
    return flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0
}