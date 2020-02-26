package com.ribbit.ui.pickers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ribbit.R
import com.ribbit.data.remote.models.loginsignup.InterestDto
import com.ribbit.extensions.gone
import com.ribbit.extensions.visible
import kotlinx.android.synthetic.main.item_picker.view.*

class PickerAdapter(context: Context, private val singleSelection: Boolean, private val callback: () -> Unit)
    : RecyclerView.Adapter<PickerAdapter.ViewHolder>() {
    private val inflater = LayoutInflater.from(context)
    private val dataItems = ArrayList<InterestDto>()
    private var prevPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_picker, parent, false))
    }

    override fun getItemCount(): Int {
        return dataItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindInstructor(dataItems[position])
    }

    inner class ViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
        init {
            itemView.rlPicker.setOnClickListener {
                if (singleSelection) {
                    val currentPosition = adapterPosition
                    if (currentPosition != prevPosition && currentPosition != RecyclerView.NO_POSITION) {
                        pickerData.selected = !pickerData.selected
                        setSelected(pickerData.selected)

                        if (prevPosition != -1) {
                            val prevPickerData = dataItems[prevPosition]
                            prevPickerData.selected = !prevPickerData.selected
                            notifyItemChanged(prevPosition)
                        }

                        prevPosition = currentPosition
                    }
                } else {
                    pickerData.selected = !pickerData.selected
                    setSelected(pickerData.selected)
                }
                callback()
            }
        }

        private lateinit var pickerData: InterestDto
        fun bindInstructor(pickerData: InterestDto) {
            this.pickerData = pickerData
            itemView.tvName.text = pickerData.name
            setSelected(pickerData.selected)
        }

        private fun setSelected(isSelected: Boolean) {
            if (isSelected) itemView.ivSelect.visible() else itemView.ivSelect.gone()
        }
    }

    fun displayData(dataItems: List<InterestDto>) {
        this.dataItems.clear()
        this.dataItems.addAll(dataItems)
        prevPosition = dataItems.indexOfFirst { it.selected }
        notifyDataSetChanged()
    }

    fun getSelectedDataItems(): List<InterestDto> = dataItems.filter { it.selected }

    fun getSelectedPickerData(): InterestDto? = dataItems.firstOrNull { it.selected }

    fun getPickerItems(): List<InterestDto> = dataItems

    fun getPrevPosition(): Int = prevPosition
}