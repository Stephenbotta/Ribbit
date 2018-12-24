package com.conversify.extensions

import android.graphics.Color
import android.graphics.Typeface
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import com.conversify.utils.CustomTypefaceSpan
import com.conversify.utils.SpannableTextClickListener

fun TextView.clickSpannable(spannableText: String, @ColorRes textColorRes: Int? = null, textTypeface: Typeface? = null, clickListener: View.OnClickListener) {
    val fullText = text.toString()
    val spannableString: Spannable = if (text is Spannable) {
        text as Spannable
    } else {
        SpannableString(fullText)
    }

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

    val startIndex = fullText.indexOf(spannableText)
    val endIndex = startIndex + spannableText.length
    spannableString.setSpan(clickable, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    movementMethod = LinkMovementMethod.getInstance()
    highlightColor = Color.TRANSPARENT
    setText(spannableString, TextView.BufferType.SPANNABLE)
}

fun TextView.clickSpannable(spannableTexts: List<String>?, @ColorRes textColorRes: Int? = null,
                            textTypeface: Typeface? = null, clickListener: SpannableTextClickListener) {
    if (spannableTexts == null || spannableTexts.isEmpty()) {
        return
    }

    val fullText = text.toString()
    val spannableString: Spannable = if (text is Spannable) {
        text as Spannable
    } else {
        SpannableString(fullText)
    }

    // Apply click spannable to all texts
    spannableTexts.forEach { spannableText ->
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
                clickListener.onSpannableTextClicked(spannableText, widget)
            }
        }

        val startIndex = fullText.indexOf(spannableText)
        val endIndex = startIndex + spannableText.length
        spannableString.setSpan(clickable, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    movementMethod = LinkMovementMethod.getInstance()
    highlightColor = Color.TRANSPARENT
    setText(spannableString, TextView.BufferType.SPANNABLE)
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

/**
 * This will only call if its not a hyperlink
 * */
fun TextView.isNonLinkClick(): Boolean {
    return selectionStart == -1 && selectionEnd == -1
}