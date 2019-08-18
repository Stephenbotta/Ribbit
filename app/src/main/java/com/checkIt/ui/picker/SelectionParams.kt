package com.checkIt.ui.picker

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SelectionParams(var mode: SelectionMode = SelectionMode.IMAGE,
                           var allowMultiple: Boolean = true,
                           var maxCount: Int = Int.MAX_VALUE,
                           var maxFileSize: Long = Long.MAX_VALUE) : Parcelable