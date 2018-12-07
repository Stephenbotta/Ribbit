package com.conversify.data.remote.aws

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import timber.log.Timber
import java.io.File

class S3Uploader(private val transferUtility: TransferUtility) {
    private val items by lazy { mutableMapOf<Int, S3UploadItem>() }

    private val transferListener = object : TransferListener {
        override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
        }

        override fun onStateChanged(id: Int, state: TransferState) {
            val item = items[id]

            if (state == TransferState.COMPLETED) {
                if (item == null) {
                    Timber.d("Upload completed, no item found for id : $id")
                } else {
                    val fileUrl = S3Utils.getResourceUrl(item.observer.key)
                    Timber.d("Upload completed, for id : $id\n$fileUrl")
                    item.uploadCompleteListener?.let { it(fileUrl) }
                    item.clear()
                    items.remove(id)
                }
            } else if (state == TransferState.FAILED ||
                    state == TransferState.WAITING_FOR_NETWORK ||
                    state == TransferState.PAUSED) {
                Timber.w("Network error for id : $id")
                item?.uploadFailedListener?.let { it(NetworkError) }
                cancelUpload(item)
            }
        }

        override fun onError(id: Int, ex: java.lang.Exception?) {
            Timber.w(ex)
            val item = items[id]
            if (item == null) {
                Timber.d("Upload error, no item found for id : $id")
            } else {
                Timber.w(ex)
                item.uploadFailedListener?.let { it(Exception(ex)) }
                cancelUpload(item)
            }
        }
    }

    fun upload(file: File, key: String = file.name): S3UploadItem {
        Timber.d("Uploading file : $file")

        val observer = transferUtility.upload(key, file)
        observer.setTransferListener(transferListener)

        val item = S3UploadItem(observer)
        items[observer.id] = item
        return item
    }

    fun clear() {
        items.values.forEach { item ->
            cancelUpload(item.observer)     // Cancel upload
            item.clear()    // Clear the listeners for this item
        }
    }

    private fun cancelUpload(item: S3UploadItem?) {
        item ?: return

        cancelUpload(item.observer)     // Cancel upload
        items.remove(item.observer.id)  // Remove the item from the items map
        item.clear()        // Clear the listeners for this item
    }

    private fun cancelUpload(observer: TransferObserver) {
        transferUtility.cancel(observer.id)
        observer.cleanTransferListener()
    }

    object NetworkError : Exception()

    class S3UploadItem(val observer: TransferObserver) {
        var uploadCompleteListener: ((String) -> Unit)? = null
        var uploadFailedListener: ((Exception) -> Unit)? = null

        fun addUploadCompleteListener(listener: (String) -> Unit): S3UploadItem {
            this.uploadCompleteListener = listener
            return this
        }

        fun addUploadFailedListener(listener: (Exception) -> Unit): S3UploadItem {
            this.uploadFailedListener = listener
            return this
        }

        fun clear() {
            uploadCompleteListener = null
            uploadFailedListener = null
        }
    }
}