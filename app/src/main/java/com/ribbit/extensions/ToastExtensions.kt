package com.ribbit.extensions

import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.ribbit.R

fun Toast.updateAndShow() {
    updateToast(R.drawable.background_toast, R.color.white)
    show()
}

fun Toast.updateToast(@DrawableRes backgroundRes: Int, @ColorRes textColorRes: Int) {
    val paddingHorizontal = view?.context?.pxFromDp(20)
    val paddingVertical = view?.context?.pxFromDp(12)
    view?.setBackgroundResource(backgroundRes)
    if (paddingHorizontal != null) {
        if (paddingVertical != null) {
            view?.setPaddingRelative(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
        }
    }
    view?.elevation  = view?.context?.pxFromDp(2)!!?.toFloat()
    val toastText = view!!.findViewById<TextView>(android.R.id.message)
    toastText.setTextColor(ContextCompat.getColor(view!!.context, textColorRes))
}