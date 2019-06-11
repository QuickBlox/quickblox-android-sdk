package com.quickblox.sample.chat.kotlin.utils.imagepick

import java.io.File


interface OnImagePickedListener {

    fun onImagePicked(requestCode: Int, file: File)

    fun onImagePickError(requestCode: Int, e: Exception)

    fun onImagePickClosed(requestCode: Int)
}