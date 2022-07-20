package com.ribbit.ui.picker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.ribbit.ui.picker.models.PickerAlbum
import com.ribbit.ui.picker.models.PickerMedia

class MediaHelper(context: Context, private val selectionParams: SelectionParams) {
    companion object {
        private val PROJECTION_PHOTOS = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.ORIENTATION,
                MediaStore.Images.Media.SIZE)

        private val PROJECTION_VIDEOS = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.BUCKET_ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DATE_TAKEN,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE)
    }

    private val contentResolver = context.contentResolver
    private val hasReadExternalStoragePermission = Build.VERSION.SDK_INT >= 23 &&
            context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private var callback: GetAlbumsCallback? = null

    fun setCallback(callback: GetAlbumsCallback) {
        this.callback = callback
    }

    fun getAlbums() {
        GetMediaAsyncTask(selectionParams.mode).execute()
    }

    interface GetAlbumsCallback {
        fun onGetAlbumsCompleted(albums: List<PickerAlbum>)
    }

    inner class GetMediaAsyncTask(private val selectionMode: SelectionMode)
        : AsyncTask<Void, Void, MutableList<PickerAlbum>>() {

        override fun doInBackground(vararg params: Void): MutableList<PickerAlbum> {
            return getAllMediaFiles(selectionMode)
        }

        override fun onPostExecute(mediaAlbumsSorted: MutableList<PickerAlbum>) {
            super.onPostExecute(mediaAlbumsSorted)
            callback?.onGetAlbumsCompleted(mediaAlbumsSorted)
        }
    }

    fun getAllMediaFiles(selectionMode: SelectionMode): MutableList<PickerAlbum> {
        val startMillis = System.currentTimeMillis()
        val mediaAlbumsSorted = mutableListOf<PickerAlbum>()
        val mediaAlbums = mutableMapOf<Int, PickerAlbum>()

        val cameraFolder: String? = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/Camera/"

        when (selectionMode) {
            SelectionMode.IMAGE -> {
                getAllPhotosFromGallery(mediaAlbums, mediaAlbumsSorted, cameraFolder)
            }

            SelectionMode.VIDEO -> {
                getAllVideosFromGallery(mediaAlbums, mediaAlbumsSorted, cameraFolder)
            }

            SelectionMode.BOTH -> {
                getAllPhotosFromGallery(mediaAlbums, mediaAlbumsSorted, cameraFolder)
                getAllVideosFromGallery(mediaAlbums, mediaAlbumsSorted, cameraFolder)
            }
        }

        // Sort all media files with date taken
        mediaAlbumsSorted.forEach { album ->
            album.media.sortWith(Comparator { photo1, photo2 ->
                return@Comparator when {
                    photo1.dateTaken < photo2.dateTaken -> 1
                    photo1.dateTaken > photo2.dateTaken -> -1
                    else -> 0
                }
            })
        }

        val endMillis = System.currentTimeMillis()
        Log.i("Total Time:", "${endMillis - startMillis} ms")
        Log.i("Total albums:", "${mediaAlbumsSorted.size}")

        return mediaAlbumsSorted
    }

    private fun getAllPhotosFromGallery(photoAlbums: MutableMap<Int, PickerAlbum>, photoAlbumsSorted: MutableList<PickerAlbum>, cameraFolder: String?) {
        var mediaCameraAlbumId: Int? = null

        var cursor: Cursor? = null
        try {
            if (Build.VERSION.SDK_INT < 23 || hasReadExternalStoragePermission) {
                cursor = MediaStore.Images.Media.query(contentResolver,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        PROJECTION_PHOTOS,
                        null,
                        null,
                        MediaStore.Images.Media.DATE_TAKEN + " DESC")

                if (cursor != null) {
                    val imageIdColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                    val bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)
                    val bucketNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                    val dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                    val dateColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                    val orientationColumn = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION)
                    val sizeColumn = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)

                    while (cursor.moveToNext()) {
                        // Limit the file size to the value in the selection parameters
                        val size = cursor.getLong(sizeColumn)
                        if (size == 0L || size > selectionParams.maxFileSize) {
                            continue
                        }

                        val imageId = cursor.getInt(imageIdColumn)
                        val bucketId = cursor.getInt(bucketIdColumn)
                        val bucketName = cursor.getString(bucketNameColumn)
                        val path: String? = cursor.getString(dataColumn)
                        val dateTaken = cursor.getLong(dateColumn)
                        val orientation = cursor.getInt(orientationColumn)

                        if (path == null || path.isEmpty()) {
                            continue
                        }

                        val pickerPhoto = PickerMedia.PickerPhoto(bucketId, imageId, dateTaken, path, orientation)

                        // Search for the album in photo albums
                        var pickerAlbum = photoAlbums[bucketId]

                        // If album doesn't exist then create it otherwise add photo to it
                        if (pickerAlbum == null) {
                            pickerAlbum = PickerAlbum(bucketId, bucketName, pickerPhoto)
                            photoAlbums[bucketId] = pickerAlbum
                            // Create camera photos album if it doesn't exist otherwise add to the album
                            if (mediaCameraAlbumId == null && cameraFolder != null && path.startsWith(cameraFolder)) {
                                photoAlbumsSorted.add(0, pickerAlbum)    // Add camera photos at the 0 index
                                mediaCameraAlbumId = bucketId   // Update camera photos album bucket id
                            } else {
                                photoAlbumsSorted.add(pickerAlbum)
                            }
                        }
                        pickerAlbum.media.add(pickerPhoto)
                    }
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        } finally {
            if (cursor != null) {
                try {
                    cursor.close()
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }
    }

    private fun getAllVideosFromGallery(videoAlbums: MutableMap<Int, PickerAlbum>, videoAlbumsSorted: MutableList<PickerAlbum>, cameraFolder: String?) {
        var mediaCameraAlbumId: Int? = null

        var cursor: Cursor? = null
        try {
            if (Build.VERSION.SDK_INT < 23 || hasReadExternalStoragePermission) {
                cursor = MediaStore.Images.Media.query(contentResolver,
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        PROJECTION_VIDEOS,
                        null,
                        null,
                        MediaStore.Video.Media.DATE_TAKEN + " DESC")

                if (cursor != null) {
                    val videoIdColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID)
                    val bucketIdColumn = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID)
                    val bucketNameColumn = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                    val dataColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
                    val dateColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN)
                    val durationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                    val sizeColumn = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)

                    while (cursor.moveToNext()) {
                        // Limit the file size to the value in the selection parameters
                        val size = cursor.getLong(sizeColumn)
                        if (size == 0L || size > selectionParams.maxFileSize) {
                            continue
                        }

                        val videoId = cursor.getInt(videoIdColumn)
                        val bucketId = cursor.getInt(bucketIdColumn)
                        val bucketName = cursor.getString(bucketNameColumn)
                        val path: String? = cursor.getString(dataColumn)
                        val dateTaken = cursor.getLong(dateColumn)
                        val duration = cursor.getInt(durationColumn)

                        if (path == null || path.isEmpty()) {
                            continue

                        }

                        val pickerVideo = PickerMedia.PickerVideo(bucketId, videoId, dateTaken, path, duration, null)

                        // Search for the album in video albums
                        var pickerAlbum = videoAlbums[bucketId]

                        // If album doesn't exist then create it otherwise add video to it
                        if (pickerAlbum == null) {
                            pickerAlbum = PickerAlbum(bucketId, bucketName, pickerVideo)
                            videoAlbums[bucketId] = pickerAlbum
                            // Create camera photos album if it doesn't exist otherwise add to the album
                            if (mediaCameraAlbumId == null && cameraFolder != null && path.startsWith(cameraFolder)) {
                                videoAlbumsSorted.add(0, pickerAlbum)    // Add camera videos at the 0 index
                                mediaCameraAlbumId = bucketId   // Update camera videos album bucket id
                            } else {
                                videoAlbumsSorted.add(pickerAlbum)
                            }
                        }
                        pickerAlbum.media.add(pickerVideo)
                    }
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        } finally {
            if (cursor != null) {
                try {
                    cursor.close()
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }
    }
}