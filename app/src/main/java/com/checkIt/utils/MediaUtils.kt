package com.checkIt.utils

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.*

object MediaUtils {
    /**
     * @param videoPath Absolute path of the video
     * @param thumbnailDirectory Directory where thumbnail needs to be saved
     * @param thumbnailKind Should be either MediaStore.Video.Thumbnails.MICRO_KIND or
     * MediaStore.Video.Thumbnails.MINI_KIND
     * */
    @JvmStatic
    fun getThumbnailFromVideo(videoPath: String, thumbnailDirectory: String, thumbnailKind: Int): File? {
        val thumbnailBitmap = ThumbnailUtils.createVideoThumbnail(videoPath, thumbnailKind)
        return getImageFileFromBitmap(thumbnailBitmap, thumbnailDirectory)
    }

    @JvmStatic
    fun getImageFileFromBitmap(bitmap: Bitmap, fileDirectory: String): File? {
        try {
            val mediaStorageDirectory = File(fileDirectory)
            if (!mediaStorageDirectory.exists()) {
                if (!mediaStorageDirectory.mkdirs()) return null
            }

            val imageFile = File(mediaStorageDirectory.path + File.separator +
                    "IMG_" + UUID.randomUUID().toString() + ".jpg")
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
            outputStream.flush()
            outputStream.close()

            return imageFile
        } catch (exception: Exception) {
            Timber.w(exception)
        }
        return null
    }
}