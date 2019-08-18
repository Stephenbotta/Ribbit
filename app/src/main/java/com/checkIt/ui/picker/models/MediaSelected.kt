package com.checkIt.ui.picker.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
data class MediaSelected(
        val mediaId: String,
        var path: String,
        val type: MediaType,
        val thumbnail: File? = null,
        var original: String? = null,
        var thumbnailPath: String? = null,
        val isUploaded: Boolean = false,
        var status: UploadStatus = UploadStatus.NEW_ADDED
) : Parcelable