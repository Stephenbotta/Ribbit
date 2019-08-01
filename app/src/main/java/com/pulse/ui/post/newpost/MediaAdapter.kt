package com.pulse.ui.post.newpost

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pulse.R
import com.pulse.extensions.gone
import com.pulse.extensions.visible
import com.pulse.ui.picker.models.MediaSelected
import com.pulse.ui.picker.models.MediaType
import com.pulse.ui.picker.models.UploadStatus
import com.pulse.utils.GlideApp
import com.pulse.utils.GlideRequests
import kotlinx.android.synthetic.main.item_selected_media.view.*

class MediaAdapter(context: Context, private val callback: Callback) : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {
    private val inflater = LayoutInflater.from(context)
    private val medias = ArrayList<MediaSelected>()
    private val glide = GlideApp.with(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_selected_media, parent, false), glide, callback)
    }

    override fun getItemCount(): Int = medias.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindMedia(medias[position])
    }

    class ViewHolder(itemView: View, private val glide: GlideRequests, callback: Callback) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.ivRemoveMedia.setOnClickListener {
                callback.removeMedia(media)
            }

            itemView.btnResend.setOnClickListener {
                callback.resendMedia(media)
            }
        }

        private lateinit var media: MediaSelected

        fun bindMedia(media: MediaSelected) {
            this.media = media

            glide.load(media.path)
                    .into(itemView.ivImage)

            if (media.type == MediaType.VIDEO) {
                itemView.ivVideo.visible()
            } else {
                itemView.ivVideo.gone()
            }

            updateUploadStatus(media.status)
        }

        private fun updateUploadStatus(status: UploadStatus) {
            when (status) {
                UploadStatus.SENDING -> {
                    itemView.progressBar.visible()
                    itemView.btnResend.isEnabled = false
                    itemView.btnResend.gone()
                }

                UploadStatus.SENT, UploadStatus.NEW_ADDED -> {
                    itemView.progressBar.gone()
                    itemView.btnResend.isEnabled = false
                    itemView.btnResend.gone()
                }

                UploadStatus.ERROR -> {
                    itemView.progressBar.gone()
                    itemView.btnResend.isEnabled = true
                    itemView.btnResend.visible()
                    itemView.btnResend.setImageResource(R.drawable.ic_share_white)
                }
            }
        }
    }

    fun addMediaFile(media: MediaSelected) {
        medias.add(media)
        notifyItemInserted(medias.size - 1)
    }

    fun addMediaFiles(medias: List<MediaSelected>) {
        val oldSize = this.medias.size
        this.medias.addAll(medias)
        notifyItemRangeInserted(oldSize, this.medias.size)
    }

    fun removeMediaFile(media: MediaSelected) {
        val position = medias.indexOfFirst { it.mediaId == media.mediaId }
        if (position != -1) {
            medias.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun notifyMediaChange(media: MediaSelected) {
        val position = medias.indexOfFirst { it.mediaId == media.mediaId }
        if (position != -1) {
            notifyItemChanged(position)
        }
    }

    fun checkIfAnyNewFileAdded(): Boolean {
        val position = medias.indexOfFirst { it.status == UploadStatus.NEW_ADDED }
        return position != -1
    }

    fun checkIfAnyFileUploading(): Boolean {
        val position = medias.indexOfFirst { it.status == UploadStatus.SENDING }
        return position != -1
    }

    fun getNewlyAddedMediaFiles(): List<MediaSelected> {
        return medias.filter { it.status == UploadStatus.NEW_ADDED }
    }

    fun getUploadedMediaFiles(): List<MediaSelected> {
        return medias.filter { it.status == UploadStatus.SENT }
    }

    fun isFilePendingToUpload(): Boolean {
        val position = medias.indexOfFirst { it.status == UploadStatus.ERROR }
        return position != -1
    }

    interface Callback {
        fun removeMedia(media: MediaSelected)
        fun resendMedia(media: MediaSelected)
    }
}