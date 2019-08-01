package com.pulse.ui.custom

import android.content.Context
import android.support.annotation.StringRes
import android.support.v4.content.res.ResourcesCompat
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.pulse.R
import timber.log.Timber

object AppToast {
    // Previously created toast. Make sure to create using the application context.
    private var previousToast: Toast? = null

    @JvmStatic
    fun shortToast(applicationContext: Context, text: CharSequence) {
        val toast = Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT)
        show(applicationContext, text, toast)
    }

    @JvmStatic
    fun shortToast(applicationContext: Context, @StringRes resId: Int) {
        val toast = Toast.makeText(applicationContext, resId, Toast.LENGTH_SHORT)
        show(applicationContext, applicationContext.getString(resId), toast)
    }

    @JvmStatic
    fun longToast(applicationContext: Context, text: CharSequence) {
        val toast = Toast.makeText(applicationContext, text, Toast.LENGTH_LONG)
        show(applicationContext, text, toast)
    }

    @JvmStatic
    fun longToast(applicationContext: Context, @StringRes resId: Int) {
        val toast = Toast.makeText(applicationContext, resId, Toast.LENGTH_LONG)
        show(applicationContext, applicationContext.getString(resId), toast)
    }

    @JvmStatic
    fun cancelPreviousToast() {
        previousToast?.cancel()
        previousToast = null
    }

    private fun show(applicationContext: Context, message: CharSequence, toast: Toast) {
        try {
            // First cancel any previous toast
            cancelPreviousToast()

            // Inflate new layout for toast
            val view = View.inflate(applicationContext, R.layout.layout_toast, null)

            // Get toast text view from newly inflated toast layout and set the text
            val typeface = ResourcesCompat.getFont(applicationContext, R.font.roboto_text_regular)
            val toastMessage = view.findViewById<TextView>(R.id.tvMessage)
            toastMessage.text = message
            toastMessage.typeface = typeface

            // Update toast's view with our layout
            toast.view = view
            toast.show()

            // Update the previous toast with the current one
            previousToast = toast
        } catch (exception: Exception) {
            Timber.e(exception)
        }
    }
}