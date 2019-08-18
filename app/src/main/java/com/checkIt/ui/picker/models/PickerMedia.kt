package com.checkIt.ui.picker.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.File

sealed class PickerMedia(val bucketId: Int,
                         val mediaId: Int,
                         val dateTaken: Long,
                         val path: String,
                         var thumbnail: File? = null,
                         var isSelected: Boolean = false) {

    @Parcelize
    data class PickerPhoto(val bucketIdPhoto: Int,
                           val photoId: Int,
                           val dateTakenPhoto: Long,
                           val pathPhoto: String,
                           val orientation: Int,
                           var thumbnailFile: File? = null,
                           var isSelectedPhoto: Boolean = false)
        : PickerMedia(bucketIdPhoto, photoId, dateTakenPhoto, pathPhoto, thumbnailFile, isSelectedPhoto), Parcelable

    @Parcelize
    data class PickerVideo(val bucketIdVideo: Int,
                           val videoId: Int,
                           var dateTakenVideo: Long,
                           val pathVideo: String,
                           val duration: Int,
                           var thumbnailFile: File? = null,
                           var isSelectedVideo: Boolean = false)
        : PickerMedia(bucketIdVideo, videoId, dateTakenVideo, pathVideo, thumbnailFile, isSelectedVideo), Parcelable
}