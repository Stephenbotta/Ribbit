package com.checkIt.utils

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.checkIt.R
import com.checkIt.ui.chat.ChatActionCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.bottom_sheet_dialog_delete_post.view.*


/**
 * contains Dialogs to be used in the application
 */
object DialogsUtil {

    fun openAlertDialog(context: Context, type: String,
                        listener: ChatActionCallback, adapterPosition: Int) {

        val contentView = View.inflate(context, R.layout.bottom_sheet_dialog_delete_post, null)
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(contentView)

        when (type) {
            "video" -> {
                contentView.groupShowPost.visibility = View.VISIBLE
                contentView.tvActionShow.text = context.getString(R.string.show_video)
                contentView.tvDelete.text = context.getString(R.string.delete_video)
            }
            "image" -> {
                contentView.groupShowPost.visibility = View.VISIBLE
                contentView.tvActionShow.text = context.getString(R.string.show_image)
                contentView.tvDelete.text = context.getString(R.string.delete_image)
            }
            "text" -> {
                contentView.tvDelete.text = context.getString(R.string.delete_message)
                contentView.groupShowPost.visibility = View.GONE
            }
        }

        bottomSheetDialog.show()

        contentView.tvDelete.setOnClickListener {
            when (type) {
                "video" -> {
                    bottomSheetDialog.dismiss()
                    openDeleteMessageDialog(context, type, listener, adapterPosition)
                }
                "image" -> {
                    bottomSheetDialog.dismiss()
                    openDeleteMessageDialog(context, type, listener, adapterPosition)
                }
                "text" -> {
                    listener.onDeleteImage(adapterPosition)
                    bottomSheetDialog.dismiss()
                }
            }

        }

        contentView.tvActionShow.setOnClickListener {
            when (type) {
                "video" -> {
                    listener.onVideoShow(adapterPosition)
                    bottomSheetDialog.dismiss()
                }
                "image" -> {
                    listener.onImageShow(adapterPosition)
                    bottomSheetDialog.dismiss()
                }
            }
        }

        contentView.tvCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }


    private fun openDeleteMessageDialog(context: Context, type: String,
                                        listener: ChatActionCallback, adapterPosition: Int) {

        val contentView = View.inflate(context, R.layout.bottom_sheet_dialog_delete_post, null)
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(contentView)

        when (type) {
            "video" -> {
                contentView.groupShowPost.visibility = View.GONE
                contentView.tvDelete.text = context.getString(R.string.delete_message)
            }
            "image" -> {
                contentView.groupShowPost.visibility = View.GONE
                contentView.tvDelete.text = context.getString(R.string.delete_message)
            }
        }

        bottomSheetDialog.show()

        contentView.tvDelete.setOnClickListener {
            when (type) {
                "video" -> {
                    bottomSheetDialog.dismiss()
                    listener.onDeleteImage(adapterPosition)
                }
                "image" -> {
                    bottomSheetDialog.dismiss()
                    listener.onDeleteImage(adapterPosition)
                }
            }

        }

        contentView.tvCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }

    fun showDialog(context: Context, layout: Int): Dialog {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(layout)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER

        dialog.window!!.attributes = lp
        dialog.setCancelable(true)
        dialog.window!!.decorView.setBackgroundResource(android.R.color.transparent)
        return dialog
    }

}