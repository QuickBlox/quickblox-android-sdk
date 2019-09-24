package com.quickblox.sample.chat.kotlin.utils.imagepick.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.quickblox.sample.chat.kotlin.utils.CAMERA_REQUEST_CODE
import com.quickblox.sample.chat.kotlin.utils.GALLERY_REQUEST_CODE
import com.quickblox.sample.chat.kotlin.utils.getLastUsedCameraFile
import com.quickblox.sample.chat.kotlin.utils.imagepick.GetFilepathFromUriTask
import com.quickblox.sample.chat.kotlin.utils.imagepick.OnImagePickedListener

private const val ARG_REQUEST_CODE = "requestCode"
private const val ARG_PARENT_FRAGMENT = "parentFragment"

class ImagePickHelperFragment : Fragment() {

    private var listener: OnImagePickedListener? = null

    companion object {
        private val TAG = ImagePickHelperFragment::class.java.simpleName

        fun getInstance(activity: FragmentActivity, requestCode: Int): ImagePickHelperFragment {
            val fragmentManager = activity.supportFragmentManager
            var fragment: ImagePickHelperFragment? = fragmentManager.findFragmentByTag(TAG) as ImagePickHelperFragment?

            if (fragment == null) {
                fragment = ImagePickHelperFragment()
                fragmentManager.beginTransaction().add(fragment, TAG).commitAllowingStateLoss()
                val args = Bundle()
                args.putInt(ARG_REQUEST_CODE, requestCode)
                fragment.arguments = args
            }
            return fragment
        }

        fun stop(fragmentManager: FragmentManager) {
            val fragment = fragmentManager.findFragmentByTag(TAG)
            fragment?.let {
                fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val tag = arguments?.getString(ARG_PARENT_FRAGMENT)
        val fragment = (context as AppCompatActivity).supportFragmentManager.findFragmentByTag(tag)

        if (fragment != null && fragment is OnImagePickedListener) {
            listener = fragment
        } else if (context is OnImagePickedListener) {
            listener = context
        }
        if (listener == null) {
            throw IllegalStateException("Either activity or fragment should implement OnImagePickedListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var intent = data
        super.onActivityResult(requestCode, resultCode, intent)
        if (isResultFromImagePick(requestCode, resultCode, intent)) {
            if (requestCode == CAMERA_REQUEST_CODE && (intent == null || intent.data == null)) {
                // Hacky way to get EXTRA_OUTPUT param to work.
                // When setting EXTRA_OUTPUT param in the camera intent there is a chance that data will return as null
                // So we just pass temporary camera file as a data, because RESULT_OK means that photo was written in the file.
                intent = Intent()
                intent.data = Uri.fromFile(getLastUsedCameraFile())
            }
            GetFilepathFromUriTask(childFragmentManager, listener, arguments!!.getInt(ARG_REQUEST_CODE)).execute(intent)
        } else {
            stop(childFragmentManager)
            listener?.onImagePickClosed(arguments!!.getInt(ARG_REQUEST_CODE))
        }
    }

    private fun isResultFromImagePick(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return resultCode == Activity.RESULT_OK && (requestCode == CAMERA_REQUEST_CODE
                || requestCode == GALLERY_REQUEST_CODE && data != null)
    }
}