package com.checkIt.extensions

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
import com.checkIt.R
import com.checkIt.data.local.UserManager
import com.checkIt.data.local.models.AppError
import com.checkIt.data.remote.socket.SocketManager
import com.checkIt.ui.custom.AppToast
import com.checkIt.ui.landing.LandingActivity
import com.checkIt.utils.AppConstants
import timber.log.Timber

fun Context.shortToast(text: CharSequence) {
    AppToast.shortToast(applicationContext, text)
}

fun Context.shortToast(@StringRes resId: Int) {
    AppToast.shortToast(applicationContext, resId)
}

fun Context.longToast(text: CharSequence) {
    AppToast.longToast(applicationContext, text)
}

fun Context.longToast(@StringRes resId: Int) {
    AppToast.longToast(applicationContext, resId)
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
    startActivity(Intent.createChooser(intent, AppConstants.TITLE_SHARE_VIA))
}

fun Context.sendInviteViaMessages(text: String) {
    val smsIntent = Intent(Intent.ACTION_VIEW)
    smsIntent.data = Uri.parse("smsto:")
    smsIntent.putExtra("sms_body", text)
    startActivity(smsIntent)
}

fun Context.sendInviteViaEmail(text: String) {
    val emailIntent = Intent(android.content.Intent.ACTION_SEND)
    emailIntent.type = "plain/text"
    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Invite")
    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, text)
    startActivity(emailIntent)
}

fun Context.sendInviteViaEmailToMultipleContacts(text: String, emailList: ArrayList<String>) {
    val emails = emailList.joinToString { it }
    Timber.d("Emails $emails")
    val emailIntent = Intent(android.content.Intent.ACTION_SENDTO, Uri.parse("mailto:$emails"))
    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Invite")
    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, text)
    startActivity(emailIntent)
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