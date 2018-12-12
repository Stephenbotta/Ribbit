package com.conversify.extensions

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.widget.TextView
import android.widget.Toast
import com.conversify.R

fun Toast.updateAndShow() {
    updateToast(R.drawable.background_toast, R.color.white)
    show()
}

fun Toast.updateToast(@DrawableRes backgroundRes: Int, @ColorRes textColorRes: Int) {
    val paddingHorizontal = view.context.pxFromDp(20)
    val paddingVertical = view.context.pxFromDp(12)
    view.setBackgroundResource(backgroundRes)
    view.setPaddingRelative(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
    view.elevation = view.context.pxFromDp(2).toFloat()
    val toastText = view.findViewById<TextView>(android.R.id.message)
    toastText.setTextColor(ContextCompat.getColor(view.context, textColorRes))
}