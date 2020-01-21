package com.quickblox.sample.chat.kotlin.utils.imagepick.fragment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.quickblox.sample.chat.kotlin.R
import com.quickblox.sample.chat.kotlin.utils.startCameraForResult
import com.quickblox.sample.chat.kotlin.utils.startFilePicker
import com.quickblox.sample.chat.kotlin.utils.startMediaPicker

private const val POSITION_GALLERY = 0
private const val POSITION_CAMERA = 1
private const val POSITION_FILE = 2

class ImageSourcePickDialogFragment : DialogFragment() {

    private var onImageSourcePickedListener: OnImageSourcePickedListener? = null

    companion object {
        fun show(fm: FragmentManager, onImageSourcePickedListener: OnImageSourcePickedListener) {
            val fragment = ImageSourcePickDialogFragment()
            fragment.setOnImageSourcePickedListener(onImageSourcePickedListener)
            fragment.show(fm, ImageSourcePickDialogFragment::class.java.simpleName)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity as Context)
        builder.setTitle(R.string.dlg_choose_file_from)
        builder.setItems(R.array.dlg_image_pick) { dialog, which ->
            when (which) {
                POSITION_GALLERY -> onImageSourcePickedListener?.onImageSourcePicked(ImageSource.GALLERY)
                POSITION_CAMERA -> onImageSourcePickedListener?.onImageSourcePicked(ImageSource.CAMERA)
                POSITION_FILE -> onImageSourcePickedListener?.onImageSourcePicked(ImageSource.FILE_STORAGE)
            }
        }
        return builder.create()
    }

    fun setOnImageSourcePickedListener(onImageSourcePickedListener: OnImageSourcePickedListener) {
        this.onImageSourcePickedListener = onImageSourcePickedListener
    }

    enum class ImageSource {
        GALLERY,
        CAMERA,
        FILE_STORAGE
    }

    class LoggableActivityImageSourcePickedListener(private val fragment: Fragment) : OnImageSourcePickedListener {
        override fun onImageSourcePicked(source: ImageSource) {
            when (source) {
                ImageSourcePickDialogFragment.ImageSource.GALLERY -> startMediaPicker(fragment)
                ImageSourcePickDialogFragment.ImageSource.CAMERA -> startCameraForResult(fragment)
                ImageSourcePickDialogFragment.ImageSource.FILE_STORAGE -> startFilePicker(fragment)
            }
        }
    }

    interface OnImageSourcePickedListener {
        fun onImageSourcePicked(source: ImageSource)
    }
}