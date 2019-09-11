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
import com.quickblox.sample.chat.kotlin.utils.startImagePicker

private const val POSITION_GALLERY = 0
private const val POSITION_CAMERA = 1

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
        builder.setTitle(R.string.dlg_choose_image_from)
        builder.setItems(R.array.dlg_image_pick) { dialog, which ->
            when (which) {
                POSITION_GALLERY -> onImageSourcePickedListener?.onImageSourcePicked(ImageSource.GALLERY)
                POSITION_CAMERA -> onImageSourcePickedListener?.onImageSourcePicked(ImageSource.CAMERA)
            }
        }
        return builder.create()
    }

    fun setOnImageSourcePickedListener(onImageSourcePickedListener: OnImageSourcePickedListener) {
        this.onImageSourcePickedListener = onImageSourcePickedListener
    }

    enum class ImageSource {
        GALLERY,
        CAMERA
    }

    class LoggableActivityImageSourcePickedListener(private val fragment: Fragment) : OnImageSourcePickedListener {
        override fun onImageSourcePicked(source: ImageSource) {
            when (source) {
                ImageSourcePickDialogFragment.ImageSource.GALLERY -> startImagePicker(fragment)
                ImageSourcePickDialogFragment.ImageSource.CAMERA -> startCameraForResult(fragment)
            }
        }
    }

    interface OnImageSourcePickedListener {
        fun onImageSourcePicked(source: ImageSource)
    }
}