package com.conversify.extensions

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.conversify.utils.CustomTypefaceSpan

fun View.hideKeyboard() {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun View.shortSnackbar(text: CharSequence) {
    Snackbar.make(this, text, Snackbar.LENGTH_SHORT).show()
}

fun View.shortSnackbar(@StringRes resId: Int) {
    Snackbar.make(this, resId, Snackbar.LENGTH_SHORT).show()
}

fun View.longSnackbar(text: CharSequence) {
    Snackbar.make(this, text, Snackbar.LENGTH_LONG).show()
}

fun View.longSnackbar(@StringRes resId: Int) {
    Snackbar.make(this, resId, Snackbar.LENGTH_LONG).show()
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.isVisible(): Boolean {
    return visibility == View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun ViewGroup.inflate(resId: Int): View {
    return LayoutInflater.from(context).inflate(resId, this, false)
}

fun TextView.spanString(string: String, color: Int) {
    val spannableText = SpannableString(text.toString())
    if (context != null)
        spannableText.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, color)),
                string.length, spannableText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    this.text = spannableText
}

fun TextView.clickSpannable(spannableText: String, @ColorRes textColorRes: Int? = null, textTypeface: Typeface? = null, clickListener: View.OnClickListener) {
    val fullText = text.toString()
    val startIndex = fullText.indexOf(spannableText)
    val spannableString = SpannableString(fullText)

    val clickable = object : ClickableSpan() {
        override fun updateDrawState(textPaint: TextPaint) {
            super.updateDrawState(textPaint)
            textPaint.isUnderlineText = false
            if (textColorRes != null) {
                textPaint.color = ContextCompat.getColor(context, textColorRes)
            }
            if (textTypeface != null) {
                textPaint.typeface = textTypeface
            }
        }

        override fun onClick(widget: View) {
            clickListener.onClick(widget)
        }
    }

    spannableString.setSpan(clickable, startIndex, startIndex + spannableText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    movementMethod = LinkMovementMethod.getInstance()
    highlightColor = Color.TRANSPARENT
    text = spannableString
}

fun TextView.typefaceSpannable(spannableText: String, textTypeface: Typeface) {
    val fullText = text.toString()
    val startIndex = fullText.indexOf(spannableText)
    val spannableString = SpannableString(fullText)

    val spannable = CustomTypefaceSpan("", textTypeface)
    spannableString.setSpan(spannable, startIndex, startIndex + spannableText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    movementMethod = LinkMovementMethod.getInstance()
    highlightColor = Color.TRANSPARENT
    text = spannableString
}

fun TextView.isEllipsized(): Boolean {
    return layout.getEllipsisCount(lineCount - 1) > 0
}