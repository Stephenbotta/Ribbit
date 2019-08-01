package com.pulse.extensions

import android.graphics.Color
import android.graphics.RectF
import android.graphics.Typeface
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.pulse.utils.CustomTypefaceSpan
import com.pulse.utils.SpannableTextClickListener

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

fun TextView.clickableSpanUnderTouch(event: MotionEvent): Boolean {
    val text = if (text is Spannable) {
        text as Spannable
    } else {
        return false
    }

    val touchedLineBounds = RectF()

    // So we need to find the location in text where touch was made, regardless of whether the TextView
    // has scrollable text. That is, not the entire text is currently visible.
    var touchX = event.x
    var touchY = event.y

    // Ignore padding.
    touchX -= totalPaddingLeft
    touchY -= totalPaddingTop

    // Account for scrollable text.
    touchX += scrollX
    touchY += scrollY

    val layout = layout
    val touchedLine = layout.getLineForVertical(touchY.toInt())
    val touchOffset = layout.getOffsetForHorizontal(touchedLine, touchX)

    touchedLineBounds.left = layout.getLineLeft(touchedLine)
    touchedLineBounds.top = layout.getLineTop(touchedLine).toFloat()
    touchedLineBounds.right = layout.getLineWidth(touchedLine) + touchedLineBounds.left
    touchedLineBounds.bottom = layout.getLineBottom(touchedLine).toFloat()

    if (touchedLineBounds.contains(touchX, touchY)) {
        // Find a ClickableSpan that lies under the touched area.
        val spans = text.getSpans(touchOffset, touchOffset, ClickableSpan::class.java)
        for (span in spans) {
            if (span is ClickableSpan) {
                return true
            }
        }
        // No ClickableSpan found under the touched location.
        return false
    } else {
        // Touch lies outside the line's horizontal bounds where no spans should exist.
        return false
    }
}