package com.quickblox.sample.chat.kotlin.utils.imagepick

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.fragment.app.FragmentManager
import com.quickblox.sample.chat.kotlin.App
import com.quickblox.sample.chat.kotlin.async.BaseAsyncTask
import com.quickblox.sample.chat.kotlin.ui.dialog.ProgressDialogFragment
import java.io.*
import java.lang.ref.WeakReference
import java.net.URLConnection
import java.net.URLDecoder

private const val BUFFER_SIZE_2_MB = 2048

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
    override fun performInBackground(vararg params: Intent): File? {
        val fileUri = params[0].data
        var file: File? = null
        fileUri?.let { uri ->
            file = getFile(uri)
        }
        return file
    }

    @Throws(Exception::class)
    private fun getFile(uri: Uri): File {
        val fileExtension = getFileExtension(uri)

        if (TextUtils.isEmpty(fileExtension)) {
            throw Exception("Didn't get file extension")
        }

        val decodedFilePath = URLDecoder.decode(uri.toString(), "UTF-8")
        var fileName = decodedFilePath.substring(decodedFilePath.lastIndexOf("/") + 1)
        if (!fileName.contains(fileExtension!!)) {
            fileName = "$fileName.$fileExtension"
        }

        var resultFile = getFileFromCache(fileName)
        if (resultFile == null) {
            resultFile = createAndWriteFileToCache(fileName, uri)
        }

        return resultFile
    }

    @Throws(Exception::class)
    private fun getFileExtension(uri: Uri): String? {
        var fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())

        val isUriSchemeContent = uri.scheme != null && uri.scheme == ContentResolver.SCHEME_CONTENT
        if (TextUtils.isEmpty(fileExtension) && isUriSchemeContent) {
            val contentResolver = App.getInstance().contentResolver
            val mimeTypeMap = MimeTypeMap.getSingleton()
            fileExtension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
        }

        val isUriSchemeFile = uri.scheme != null && uri.scheme == ContentResolver.SCHEME_FILE
        if (TextUtils.isEmpty(fileExtension) && isUriSchemeFile) {
            val path: String = uri.path as String
            val fis = FileInputStream(File(path))
            val bis = BufferedInputStream(fis)
            val sourceFileType = URLConnection.guessContentTypeFromStream(bis)
            fileExtension = sourceFileType.substring(sourceFileType.lastIndexOf("/") + 1)
        }

        return fileExtension
    }

    private fun getFileFromCache(fileName: String): File? {
        var foundFile: File? = null
        val dir = File(App.getInstance().cacheDir.absolutePath)
        if (dir.exists()) {
            for (file in dir.listFiles()) {
                if (file.name == fileName) {
                    foundFile = file
                    break
                }
            }
        }
        return foundFile
    }

    @Throws(Exception::class)
    private fun createAndWriteFileToCache(fileName: String, uri: Uri): File {
        val resultFile = File(App.getInstance().cacheDir, fileName)
        val parcelFileDescriptor = App.getInstance().contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor

        val inputStream: InputStream = FileInputStream(fileDescriptor)

        val bis = BufferedInputStream(inputStream)
        val bos = BufferedOutputStream(FileOutputStream(resultFile))

        try {
            val buf = ByteArray(BUFFER_SIZE_2_MB)
            var length: Int
            while (bis.read(buf).also { length = it } > 0) {
                bos.write(buf, 0, length)
            }
        } catch (e: Exception) {
            throw Exception("Error create and write file in a cache")
        } finally {
            parcelFileDescriptor.close()
            bis.close()
            bos.close()
        }
        return resultFile
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