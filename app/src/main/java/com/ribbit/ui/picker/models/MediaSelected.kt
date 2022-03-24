package com.ribbit.ui.picker.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
data class MediaSelected(
        val mediaId: String,
        var path: String,
        val type: MediaType? = null,
        val thumbnail: File? = null,
        var original: String? = null,
        var thumbnailPath: String? = null,
        var status: UploadStatus = UploadStatus.NEW_ADDED
) : Parcelable