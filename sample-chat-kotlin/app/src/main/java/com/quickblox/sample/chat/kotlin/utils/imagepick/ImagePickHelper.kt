package com.quickblox.sample.chat.kotlin.utils.imagepick

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.quickblox.sample.chat.kotlin.utils.imagepick.fragment.ImagePickHelperFragment
import com.quickblox.sample.chat.kotlin.utils.imagepick.fragment.ImageSourcePickDialogFragment


fun pickAnImage(activity: FragmentActivity, requestCode: Int) {
    val imagePickHelperFragment = ImagePickHelperFragment.getInstance(activity, requestCode)
    showImageSourcePickerDialog(activity.supportFragmentManager, imagePickHelperFragment)
}

private fun showImageSourcePickerDialog(fragmentManager: FragmentManager, fragment: ImagePickHelperFragment) {
    ImageSourcePickDialogFragment.show(fragmentManager,
            ImageSourcePickDialogFragment.LoggableActivityImageSourcePickedListener(fragment))
}