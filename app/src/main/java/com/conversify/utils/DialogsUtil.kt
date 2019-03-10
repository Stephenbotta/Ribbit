package com.conversify.utils

import android.app.Dialog
import android.content.Context
import android.support.design.widget.BottomSheetDialog
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.conversify.R
import com.conversify.ui.chat.ChatActionCallback
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
            }
            "image" -> {
                contentView.groupShowPost.visibility = View.VISIBLE
                contentView.tvActionShow.text = context.getString(R.string.show_image)
            }
            "text" -> {
                contentView.groupShowPost.visibility = View.GONE
            }
        }

        bottomSheetDialog.show()

        contentView.tvDelete.setOnClickListener {
            listener.onDeleteImage(adapterPosition)
            bottomSheetDialog.dismiss()
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