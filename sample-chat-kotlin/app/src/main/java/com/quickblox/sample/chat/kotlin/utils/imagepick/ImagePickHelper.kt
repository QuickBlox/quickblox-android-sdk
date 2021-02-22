package com.quickblox.sample.chat.kotlin.utils.imagepick

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.quickblox.sample.chat.kotlin.utils.imagepick.fragment.MediaPickHelperFragment
import com.quickblox.sample.chat.kotlin.utils.imagepick.fragment.MediaSourcePickDialogFragment


fun pickAnImage(activity: FragmentActivity, requestCode: Int) {
    val mediaPickHelperFragment = MediaPickHelperFragment.getInstance(activity, requestCode)
    showImageSourcePickerDialog(activity.supportFragmentManager, mediaPickHelperFragment)
}

private fun showImageSourcePickerDialog(fragmentManager: FragmentManager, fragment: MediaPickHelperFragment) {
    MediaSourcePickDialogFragment.show(fragmentManager,
            MediaSourcePickDialogFragment.ImageSourcePickedListener(fragment))
}