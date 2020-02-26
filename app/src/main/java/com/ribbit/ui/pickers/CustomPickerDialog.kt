package com.ribbit.ui.pickers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.SimpleItemAnimator
import com.ribbit.R
import com.ribbit.data.remote.models.loginsignup.InterestDto
import com.ribbit.extensions.getScreenHeight
import com.ribbit.extensions.getScreenWidth
import com.ribbit.extensions.updateAlphaLevel
import kotlinx.android.synthetic.main.layout_picker.*

class CustomPickerDialog(private val singleSelection: Boolean, private val data: List<InterestDto>) : DialogFragment() {
    companion object {
        const val TAG = "CustomPickerDialog"
    }

    private lateinit var adapter: PickerAdapter
    private var callback: PickerSelectionCallback? = null

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout((requireActivity().getScreenWidth() * 0.9).toInt(), (requireActivity().getScreenHeight() * 0.9).toInt())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvHeader?.text = view.context.getString(R.string.converse_post_label_select_interest)
        btnDone.isEnabled = false
        btnDone.updateAlphaLevel()

        adapter = PickerAdapter(view.context, singleSelection) {
            validData()
        }
        adapter.displayData(data)
        rvPicker?.adapter = adapter
        btnDone?.isEnabled = adapter.getPrevPosition() != -1
        (rvPicker?.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        ivClose?.setOnClickListener {
            dismiss()
        }

        btnDone?.setOnClickListener {
            if (singleSelection) {
                val pickerData = adapter.getSelectedPickerData() ?: return@setOnClickListener
                callback?.setSelectedItem(pickerData)
                dismiss()
            } else {
                val selectedDataItems = adapter.getSelectedDataItems()
                callback?.setSelectedItems(selectedDataItems)
                dismiss()
            }
        }
    }

    private fun validData() {
        btnDone?.isEnabled = if (singleSelection) {
            when {
                adapter.getSelectedPickerData() == null -> false
                else -> true
            }
        } else {
            when {
                adapter.getSelectedDataItems().isEmpty() -> false
                else -> true
            }
        }
        btnDone.updateAlphaLevel()
    }

    fun setCallback(callback: PickerSelectionCallback) {
        this.callback = callback
    }

    interface PickerSelectionCallback {
        fun setSelectedItems(selectedDataItems: List<InterestDto>)
        fun setSelectedItem(item: InterestDto)
    }
}