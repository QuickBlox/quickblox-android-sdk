package com.quickblox.sample.chat.kotlin.utils.imagepick

import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.FragmentManager
import com.quickblox.sample.chat.kotlin.App
import com.quickblox.sample.chat.kotlin.async.BaseAsyncTask
import com.quickblox.sample.chat.kotlin.ui.dialog.ProgressDialogFragment
import com.quickblox.sample.chat.kotlin.utils.getFilePath
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference

private const val SCHEME_CONTENT = "content"
private const val SCHEME_CONTENT_GOOGLE = "content://com.google.android"
private const val SCHEME_FILE = "file"

class GetFilepathFromUriTask(fragmentManager: FragmentManager,
                             private val listener: OnImagePickedListener?,
                             private val requestCode: Int) : BaseAsyncTask<Intent, Void, File>() {

    private val fragmentManagerWeakRef: WeakReference<FragmentManager> = WeakReference(fragmentManager)

    override fun onPreExecute() {
        super.onPreExecute()
        fragmentManagerWeakRef.get()?.let {
            ProgressDialogFragment.show(it)
        }
    }

    @Throws(Exception::class)
    override fun performInBackground(vararg params: Intent): File {
        val data = params[0]

        var imageFilePath: String? = null
        val uri = data.data
        val uriScheme = uri?.scheme

        val isFromGoogleApp = uri?.toString()?.startsWith(SCHEME_CONTENT_GOOGLE) ?: false
        val isKitKatAndUpper = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        if (SCHEME_CONTENT.equals(uriScheme, ignoreCase = true) && !isFromGoogleApp && !isKitKatAndUpper && uri != null) {
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = App.getInstance().contentResolver.query(uri, filePathColumn, null, null, null)
            cursor?.let {
                if (it.count > 0) {
                    it.moveToFirst()
                    val columnIndex = it.getColumnIndex(filePathColumn[0])
                    imageFilePath = it.getString(columnIndex)
                }
                cursor.close()
            }
        } else if (SCHEME_FILE.equals(uriScheme, ignoreCase = true)) {
            imageFilePath = uri?.path
        } else {
            uri?.let {
                imageFilePath = getFilePath(App.getInstance(), it)
            }
        }

        if (TextUtils.isEmpty(imageFilePath)) {
            throw IOException("Can't find a filepath for URI " + uri?.toString())
        }

        return File(imageFilePath)
    }

    override fun onResult(result: File?) {
        hideProgress()
        Log.w(GetFilepathFromUriTask::class.java.simpleName, "onResult listener = $listener")
        result?.let {
            listener?.onImagePicked(requestCode, result)
        }
    }

    override fun onException(e: Exception) {
        hideProgress()
        Log.w(GetFilepathFromUriTask::class.java.simpleName, "onException listener = $listener")
        listener?.onImagePickError(requestCode, e)
    }

    private fun hideProgress() {
        fragmentManagerWeakRef.get()?.let {
            ProgressDialogFragment.hide(it)
        }
    }
}