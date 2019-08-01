package com.pulse.ui.picker.models

data class PickerAlbum(val bucketId: Int,
                       val bucketName: String,
                       var coverPhoto: PickerMedia,
                       val media: ArrayList<PickerMedia> = ArrayList())