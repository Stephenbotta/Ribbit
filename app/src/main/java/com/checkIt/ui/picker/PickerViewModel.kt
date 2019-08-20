package com.checkIt.ui.picker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.checkIt.ui.picker.models.PickerAlbum
import com.checkIt.ui.picker.models.PickerMedia

class PickerViewModel(application: Application) : AndroidViewModel(application), MediaHelper.GetAlbumsCallback {
    private val albumsLiveData by lazy { MutableLiveData<List<PickerAlbum>>() }
    private val mediaCountLiveData by lazy { MutableLiveData<Int>() }

    private val mediaHelper by lazy { MediaHelper(application, selectionParams).apply { setCallback(this@PickerViewModel) } }
    private val selectedMedia by lazy { mutableListOf<PickerMedia>() }

    private lateinit var selectionParams: SelectionParams

    fun getAlbumsLiveData(): LiveData<List<PickerAlbum>> = albumsLiveData

    fun getMediaCountLiveData(): LiveData<Int> = mediaCountLiveData

    fun start(selectionParams: SelectionParams) {
        this.selectionParams = selectionParams
    }

    fun getAlbums() {
        mediaHelper.getAlbums()
    }

    fun getAlbum(bucketId: Int): PickerAlbum? {
        return albumsLiveData.value?.find { it.bucketId == bucketId }
    }

    override fun onGetAlbumsCompleted(albums: List<PickerAlbum>) {
        albumsLiveData.value = albums
    }

    fun mediaClicked(photo: PickerMedia) {
        if (photo.isSelected) {
            selectedMedia.add(photo)
        } else {
            selectedMedia.remove(photo)
        }
        mediaCountLiveData.value = selectedMedia.size
    }

    fun isValidToAddMedia(): Boolean = selectedMedia.size < selectionParams.maxCount

    fun getSelectedMedias(): List<PickerMedia> = selectedMedia

    fun allowMultipleSelection(): Boolean = selectionParams.allowMultiple
}