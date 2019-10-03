package com.ribbit.extensions

import android.widget.EditText

fun EditText.setSelectionAtEnd() {
    setSelection(text?.length ?: 0)
}