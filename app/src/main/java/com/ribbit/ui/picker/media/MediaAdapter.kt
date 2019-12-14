package com.ribbit.ui.picker.media

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.ribbit.R
import com.ribbit.extensions.gone
import com.ribbit.extensions.visible
import com.ribbit.ui.picker.models.PickerMedia
import com.ribbit.utils.DateTimeUtils
import kotlinx.android.synthetic.main.item_picker_media.view.*

class MediaAdapter(context: Context,
                   private val allowMultipleSelection: Boolean,
                   private val glide: RequestManager,
                   private val callback: Callback) : androidx.recyclerview.widget.RecyclerView.Adapter<MediaAdapter.ViewHolder>() {
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

    class ViewHolder(itemView: View, allowMultipleSelection: Boolean, private val glide: RequestManager, callback: Callback) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
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
                    itemView.tvQuizNo.gone()
                    itemView.ivVideo.gone()
                }

                is PickerMedia.PickerVideo -> {
                    itemView.tvQuizNo.visible()
                    itemView.ivVideo.visible()
                    itemView.tvQuizNo.text = DateTimeUtils.formatMillisToDuration(media.duration.toLong())
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