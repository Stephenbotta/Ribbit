package com.checkIt.utils

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.checkIt.R
import com.checkIt.data.remote.ApiConstants
import com.checkIt.data.remote.models.chat.ChatMessageDto
import com.checkIt.extensions.gone
import com.checkIt.extensions.visible
import com.checkIt.ui.chat.ChatActionCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.bottom_sheet_dialog_delete_post.view.*

/**
 * contains Dialogs to be used in the application
 */
object DialogsUtil {
    fun openAlertDialog(context: Context, chatMessage: ChatMessageDto,
                        listener: ChatActionCallback) {

        val contentView = View.inflate(context, R.layout.bottom_sheet_dialog_delete_post, null)
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(contentView)

        when (chatMessage.details?.type) {
            ApiConstants.MESSAGE_TYPE_VIDEO -> {
                contentView.groupShowPost.visible()
                contentView.tvActionShow.text = context.getString(R.string.show_video)
                contentView.tvDelete.text = context.getString(R.string.delete_video)
            }
            ApiConstants.MESSAGE_TYPE_IMAGE -> {
                contentView.groupShowPost.visible()
                contentView.tvActionShow.text = context.getString(R.string.show_image)
                contentView.tvDelete.text = context.getString(R.string.delete_image)
            }
            ApiConstants.MESSAGE_TYPE_TEXT -> {
                contentView.tvDelete.text = context.getString(R.string.delete_message)
                contentView.groupShowPost.gone()
            }
            ApiConstants.MESSAGE_TYPE_GIF -> {
                contentView.groupShowPost.visible()
                contentView.tvActionShow.text = context.getString(R.string.show_gif)
                contentView.tvDelete.text = context.getString(R.string.delete_gif)
            }
        }

        bottomSheetDialog.show()

        contentView.tvDelete.setOnClickListener {
            when (chatMessage.details?.type) {
                ApiConstants.MESSAGE_TYPE_VIDEO -> {
                    bottomSheetDialog.dismiss()
                    openDeleteMessageDialog(context, chatMessage, listener)
                }
                ApiConstants.MESSAGE_TYPE_IMAGE, ApiConstants.MESSAGE_TYPE_GIF -> {
                    bottomSheetDialog.dismiss()
                    openDeleteMessageDialog(context, chatMessage, listener)
                }
                ApiConstants.MESSAGE_TYPE_TEXT -> {
                    listener.onDeleteImage(chatMessage)
                    bottomSheetDialog.dismiss()
                }
            }
        }

        contentView.tvActionShow.setOnClickListener {
            when (chatMessage.details?.type) {
                ApiConstants.MESSAGE_TYPE_VIDEO -> {
                    listener.onVideoShow(chatMessage)
                    bottomSheetDialog.dismiss()
                }
                ApiConstants.MESSAGE_TYPE_IMAGE, ApiConstants.MESSAGE_TYPE_GIF -> {
                    listener.onImageShow(chatMessage)
                    bottomSheetDialog.dismiss()
                }
            }
        }

        contentView.tvCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }


    private fun openDeleteMessageDialog(context: Context, chatMessage: ChatMessageDto,
                                        listener: ChatActionCallback) {

        val contentView = View.inflate(context, R.layout.bottom_sheet_dialog_delete_post, null)
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(contentView)

        if (chatMessage.details?.type == ApiConstants.MESSAGE_TYPE_VIDEO ||
                chatMessage.details?.type == ApiConstants.MESSAGE_TYPE_IMAGE ||
                chatMessage.details?.type == ApiConstants.MESSAGE_TYPE_GIF) {
            contentView.groupShowPost.gone()
            contentView.tvDelete.text = context.getString(R.string.delete_message)
        }

        bottomSheetDialog.show()

        contentView.tvDelete.setOnClickListener {
            if (chatMessage.details?.type == ApiConstants.MESSAGE_TYPE_VIDEO ||
                    chatMessage.details?.type == ApiConstants.MESSAGE_TYPE_IMAGE ||
                    chatMessage.details?.type == ApiConstants.MESSAGE_TYPE_GIF) {
                bottomSheetDialog.dismiss()
                listener.onDeleteImage(chatMessage)
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
        lp.copyFrom(dialog.window?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER

        dialog.window?.attributes = lp
        dialog.setCancelable(true)
        dialog.window?.decorView?.setBackgroundResource(android.R.color.transparent)
        return dialog
    }

}