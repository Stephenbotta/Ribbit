package com.pulse.ui.picker.media

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.pulse.R
import com.pulse.extensions.gone
import com.pulse.extensions.visible
import com.pulse.ui.picker.models.PickerMedia
import com.pulse.utils.DateTimeUtils
import kotlinx.android.synthetic.main.item_picker_media.view.*

class MediaAdapter(context: Context,
                   private val allowMultipleSelection: Boolean,
                   private val glide: RequestManager,
                   private val callback: Callback) : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {
    private val inflater = LayoutInflater.from(context)
    private val medias = mutableListOf<PickerMedia>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_picker_media, parent, false), allowMultipleSelection, glide, callback)
    }

    override fun getItemCount() = medias.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(medias[position])
    }

    fun displayMedias(medias: List<PickerMedia>) {
        this.medias.addAll(medias)
        notifyItemRangeInserted(0, medias.size)
    }

    class ViewHolder(itemView: View, allowMultipleSelection: Boolean, private val glide: RequestManager, callback: Callback) : RecyclerView.ViewHolder(itemView) {
        init {
            if (allowMultipleSelection) {
                itemView.ivSelected.visible()
            } else {
                itemView.ivSelected.gone()
            }

            itemView.setOnClickListener {
                if (allowMultipleSelection) {
                    if (media.isSelected) {
                        media.isSelected = !media.isSelected
                        setSelected(media.isSelected)
                        callback.onMediaClicked(media)
                    } else if (callback.isValidToAddMedia()) {
                        media.isSelected = !media.isSelected
                        setSelected(media.isSelected)
                        callback.onMediaClicked(media)
                    }
                } else {
                    callback.onMediaSelected(media)
                }
            }
        }

        private lateinit var media: PickerMedia

        fun bind(media: PickerMedia) {
            this.media = media
            glide.load(media.path)
                    .into(itemView.ivPhoto)

            setSelected(media.isSelected)

            when (media) {
                is PickerMedia.PickerPhoto -> {
                    itemView.tvDuration.gone()
                    itemView.ivVideo.gone()
                }

                is PickerMedia.PickerVideo -> {
                    itemView.tvDuration.visible()
                    itemView.ivVideo.visible()
                    itemView.tvDuration.text = DateTimeUtils.formatMillisToDuration(media.duration.toLong())
                }
            }
        }

        private fun setSelected(isSelected: Boolean) {
            if (isSelected) {
                itemView.ivSelected.setImageResource(R.drawable.ic_tick)
            } else {
                itemView.ivSelected.setImageResource(R.drawable.ic_add_gray_top_right)
            }
        }
    }

    interface Callback {
        fun onMediaClicked(media: PickerMedia)
        fun isValidToAddMedia(): Boolean
        fun onMediaSelected(media: PickerMedia)
    }
}