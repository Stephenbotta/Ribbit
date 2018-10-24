package com.conversify.extensions

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.Uri
import android.support.annotation.StringRes
import android.util.Patterns
import android.view.WindowManager
import android.widget.Toast
import com.conversify.R
import com.conversify.data.local.UserManager
import com.conversify.data.local.models.AppError
import timber.log.Timber

fun Context.shortToast(text: CharSequence) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Context.shortToast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}

fun Context.longToast(text: CharSequence) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}

fun Context.longToast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
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
            startLoginSignUpWithClear()
        }

        is AppError.ApiFailure -> longToast(error.message)
    }
}

fun Context.startLoginSignUpWithClear() {
    clearNotifications()
    UserManager.removeProfile()

    // todo start login sign up activity
    /*val intent = Intent(this, LoginSignUpActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    startActivity(intent)*/
}