package com.conversify.utils

import android.app.DownloadManager
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.webkit.MimeTypeMap
import timber.log.Timber
import java.io.File

object FileUtils {
    const val MIME_TYPE_ALL = "*/*"
    const val MIME_TYPE_IMAGE = "image/*"
    const val MIME_TYPE_JPG = "image/jpeg"
    const val MIME_TYPE_PNG = "image/png"
    const val MIME_TYPE_VIDEO = "video/*"
    const val MIME_TYPE_PDF = "application/pdf"
    const val MIME_TYPE_DOC = "application/msword"
    const val MIME_TYPE_TEXT = "text/plain"

    /**
     * https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
     * */
    @JvmStatic
    fun getPath(context: Context, uri: Uri): String? {
        when {
            // DocumentProvider
            DocumentsContract.isDocumentUri(context, uri) -> {
                when {
                    // ExternalStorageProvider
                    isExternalStorageDocument(uri) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":")
                        if (split.size < 2) return null

                        val type = split[0]
                        if ("primary".equals(type, true)) {
                            return "${Environment.getExternalStorageDirectory()}/${split[1]}"
                        }
                    }

                    // DownloadsProvider
                    isDownloadsDocument(uri) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), docId.toLong())
                        return getDataColumn(context, contentUri, null, null)
                    }

                    // MediaProvider
                    isMediaDocument(uri) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":")
                        if (split.size < 2) return null

                        val type = split[0]
                        val contentUri = when (type) {
                            "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI

                            "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI

                            "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

                            else -> return null
                        }

                        val selection = "_id=?"
                        val selectionArgs = arrayOf(split[1])
                        return getDataColumn(context, contentUri, selection, selectionArgs)
                    }

                    else -> return null
                }
            }

            // MediaStore and general
            uri.scheme == "content" -> {
                // Return the remote address
                return if (isGooglePhotosUri(uri)) {
                    uri.lastPathSegment
                } else {
                    getDataColumn(context, uri, null, null)
                }
            }

            uri.scheme == "file" -> return uri.path

            else -> return null
        }

        return null
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } catch (exception: Exception) {
            Timber.w(exception)
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    @JvmStatic
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    fun getMimeType(file: File): String? {
        val extension = getExtension(file.name) ?: return null

        return if (extension.isNotEmpty())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1))
        else
            "application/octet-stream"
    }

    /**
     * Gets the extension of a file name, like ".png" or ".jpg".
     *
     * @param uri
     * @return Extension including the dot("."); "" if there is no extension;
     * null if uri was null.
     */
    private fun getExtension(uri: String?): String? {
        if (uri == null) {
            return null
        }

        val dot = uri.lastIndexOf(".")
        return if (dot >= 0) {
            uri.substring(dot)
        } else {
            // No extension.
            ""
        }
    }

    fun openPdfFile(context: Context, file: File) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.fromFile(file), MIME_TYPE_PDF)
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            context.startActivity(intent)
        } catch (exception: Exception) {
            Timber.w(exception)
        }
    }

    fun openPdfUrl(context: Context, pdfUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            context.startActivity(intent)
        } catch (exception: Exception) {
            Timber.w(exception)
        }
    }

    fun downloadFile(context: Context, fileUrl: String, fileName: String) {
        val downloadManager = context.applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(fileUrl))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                .setTitle(fileName)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        downloadManager.enqueue(request)
    }

    fun showFilePicker(fragment: Fragment, requestCode: Int, mimeTypes: Array<String> = arrayOf(MIME_TYPE_ALL)) {
        try {
            fragment.startActivityForResult(getDocumentPickerIntent(mimeTypes), requestCode)
        } catch (exception: Exception) {
            Timber.w(exception)
        }
    }

    fun showFilePicker(activity: AppCompatActivity, requestCode: Int, mimeTypes: Array<String> = arrayOf(MIME_TYPE_ALL)) {
        try {
            activity.startActivityForResult(getDocumentPickerIntent(mimeTypes), requestCode)
        } catch (exception: Exception) {
            Timber.w(exception)
        }
    }

    private fun getDocumentPickerIntent(mimeTypes: Array<String>): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = MIME_TYPE_ALL
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        return intent
    }

    @JvmStatic
    fun getSelectedFileFromResult(context: Context, data: Intent?): File? {
        return if (data != null) {
            val fileUri = data.data ?: return null
            val filePath = getPath(context, fileUri) ?: return null
            return File(filePath)
        } else {
            Timber.d("Result data is null")
            null
        }
    }

    @JvmStatic
    fun getAppCacheDirectory(context: Context): File {
        return context.externalCacheDir ?: context.cacheDir
    }

    @JvmStatic
    fun getAppCacheDirectoryPath(context: Context): String {
        return getAppCacheDirectory(context).absolutePath
    }

    @JvmStatic
    fun isLowStorage(context: Context): Boolean {
        return getAppCacheDirectory(context).usableSpace < AppConstants.MINIMUM_FREE_SPACE
    }
}