package com.pulse.ui.chat

interface ChatActionCallback {
    fun onDeleteImage(position: Int)
    fun onImageShow(position: Int)
    fun onVideoShow(position: Int)
}