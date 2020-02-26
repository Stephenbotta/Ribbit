package com.ribbit.ui.picker.albums

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.ribbit.R
import com.ribbit.ui.picker.models.PickerAlbum
import kotlinx.android.synthetic.main.item_picker_album.view.*

class AlbumsAdapter(context: Context, private val glide: RequestManager, private val callback: Callback)
    : RecyclerView.Adapter<AlbumsAdapter.ViewHolder>() {
    private val inflater = LayoutInflater.from(context)
    private val albums = mutableListOf<PickerAlbum>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_picker_album, parent, false), glide, callback)
    }

    override fun getItemCount() = albums.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(albums[position])
    }

    fun displayAlbums(albums: List<PickerAlbum>) {
        this.albums.addAll(albums)
        notifyItemRangeInserted(0, albums.size)
    }

    class ViewHolder(itemView: View, private val glide: RequestManager,
                     callback: Callback) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener { callback.onAlbumClicked(album) }
        }

        private lateinit var album: PickerAlbum

        fun bind(album: PickerAlbum) {
            this.album = album

            itemView.apply {
                glide.load(album.coverPhoto.path)
                        .into(ivAlbum)
                tvAlbumName.text = album.bucketName
                tvPhotoCount.text = album.media.size.toString()
            }
        }
    }

    interface Callback {
        fun onAlbumClicked(album: PickerAlbum)
    }
}