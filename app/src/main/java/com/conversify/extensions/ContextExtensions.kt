package com.conversify.extensions

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.support.annotation.StringRes
import android.util.Patterns
import android.view.WindowManager
import android.widget.Toast
import com.conversify.R
import com.conversify.data.local.UserManager
import com.conversify.data.local.models.AppError
import com.conversify.data.remote.socket.SocketManager
import com.conversify.ui.landing.LandingActivity
import timber.log.Timber

@SuppressLint("ShowToast")
fun Context.shortToast(text: CharSequence) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).updateAndShow()
}

@SuppressLint("ShowToast")
fun Context.shortToast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).updateAndShow()
}

@SuppressLint("ShowToast")
fun Context.longToast(text: CharSequence) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).updateAndShow()
}

@SuppressLint("ShowToast")
fun Context.longToast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_LONG).updateAndShow()
}

fun Context.clearNotifications() {
    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
}

fun Context.isNetworkActive(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetworkInfo
    return activeNetwork != null && activeNetwork.isConnected
}

fun Context.isNetworkActiveWithMessage(): Boolean {
    val isActive = isNetworkActive()

    if (!isActive) {
        shortToast(R.string.error_check_internet_connection)
    }

    return isActive
}

fun Context.sendEmail(email: String) {
    val emailIntent = Intent(Intent.ACTION_SENDTO)
    emailIntent.data = Uri.parse("mailto:$email")
    startActivity(emailIntent)
}

fun Context.shareText(text: String) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.putExtra(Intent.EXTRA_TEXT, text)
    intent.type = "text/plain"
    startActivity(intent)
}

fun Context.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(intent)
}

fun Context.dialPhone(phoneNumber: String) {
    try {
        if (Patterns.PHONE.matcher(phoneNumber).matches()) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null))
            startActivity(intent)
        } else {
            Timber.w("Invalid phone number : $phoneNumber")
        }
    } catch (exception: Exception) {
        Timber.w(exception)
    }
}

fun Context.sendSms(phoneNumber: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phoneNumber, null))
    startActivity(intent)
}

fun Context.dpFromPx(px: Int): Int {
    return (px / resources.displayMetrics.density).toInt()
}

fun Context.pxFromDp(dp: Int): Int {
    return (dp * resources.displayMetrics.density).toInt()
}

fun Context.getScreenWidth(): Int {
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val size = Point()
    windowManager.defaultDisplay.getSize(size)
    return size.x
}

fun Context.getScreenHeight(): Int {
    val w = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val size = Point()
    w.defaultDisplay.getSize(size)
    return size.y
}

fun Context.handleError(error: AppError?) {
    error ?: return

    Timber.w(error.toString())

    when (error) {
        is AppError.ApiError -> longToast(error.message)

        is AppError.ApiUnauthorized -> {
            longToast(error.message)
            startLandingWithClear()
        }

        is AppError.ApiFailure -> longToast(error.message)
    }
}

fun Context.startLandingWithClear() {
    clearNotifications()
    UserManager.removeProfile()
    SocketManager.destroy()

    val intent = Intent(this, LandingActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    startActivity(intent)
}

fun Context.isGpsEnabled(): Boolean {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return try {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    } catch (exception: Exception) {
        Timber.w(exception)
        false
    }
}