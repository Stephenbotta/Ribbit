package com.checkIt.ui.picker

import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import java.lang.ref.WeakReference

class PickerBuilder {
    companion object {
        const val REQ_CODE_PICKER = 123

        fun with(activity: AppCompatActivity): PickerBuilder {
            return PickerBuilder(activity)
        }

        fun with(fragment: Fragment): PickerBuilder {
            return PickerBuilder(fragment)
        }
    }

    private var activity: WeakReference<AppCompatActivity>? = null
    private var fragment: WeakReference<Fragment>? = null

    private constructor(activity: AppCompatActivity) {
        this.activity = WeakReference(activity)
    }

    private constructor(fragment: Fragment) {
        this.fragment = WeakReference(fragment)
    }

    fun mode(selectionMode: SelectionMode): PickerSelection {
        return PickerSelection(this, selectionMode)
    }

    class PickerSelection(private val pickerBuilder: PickerBuilder,
                          selectionMode: SelectionMode) {
        private val selectionParams = SelectionParams(selectionMode)

        fun setMaxCount(maxCount: Int): PickerSelection {
            selectionParams.maxCount = maxCount
            return this
        }

        fun setMaxFileSize(fileSizeInBytes: Long): PickerSelection {
            selectionParams.maxFileSize = fileSizeInBytes
            return this
        }

        fun setMultipleSelection(allowMultiple: Boolean): PickerSelection {
            selectionParams.allowMultiple = allowMultiple
            return this
        }

        fun pick(requestCode: Int = REQ_CODE_PICKER) {
            val fragment = pickerBuilder.fragment?.get()
            val activity = pickerBuilder.activity?.get()
            when {
                fragment != null -> {
                    fragment.startActivityForResult(getIntent(fragment.requireContext()), requestCode)
                }

                activity != null -> {
                    activity.startActivityForResult(getIntent(activity), requestCode)
                }

                else -> throw IllegalStateException("Please provide a Fragment or Activity to the picker using PickerBuilder.with()")
            }
        }

        private fun getIntent(context: Context): Intent {
            return PickerActivity.getIntent(context, selectionParams)
        }
    }
}