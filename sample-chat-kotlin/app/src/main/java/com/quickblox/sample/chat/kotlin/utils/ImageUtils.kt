package com.quickblox.sample.chat.kotlin.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.CursorLoader
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.quickblox.sample.chat.kotlin.App
import com.quickblox.sample.chat.kotlin.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


private const val CAMERA_FILE_NAME_PREFIX = "CAMERA_"
private const val FILES_MIME = "*/*"
private const val VIDEO_OR_IMAGE_MIME = "image/* video/*"
private const val IMAGE_MIME = "image/*"

private const val EXTERNAL_STORAGE_URI = "com.android.externalstorage.documents"
private const val DOWNLOADS_URI = "com.android.providers.downloads.documents"
private const val MEDIA_URI = "com.android.providers.media.documents"
private const val GOOGLE_PHOTOS_URI = "com.google.android.apps.photos.content"
private const val GOOGLE_DOCS_URI = "com.google.android.apps.docs.storage"

const val GALLERY_REQUEST_CODE = 183
const val CAMERA_REQUEST_CODE = 212
const val FILE_REQUEST_CODE = 189

private val isKitkatSupportDevice = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

private fun isExtStorageDocument(uri: Uri): Boolean {
    return EXTERNAL_STORAGE_URI == uri.authority
}

private fun isDownloadsDocument(uri: Uri): Boolean {
    return DOWNLOADS_URI == uri.authority
}

private fun isMediaDocument(uri: Uri): Boolean {
    return MEDIA_URI == uri.authority
}

private fun isGooglePhotosUri(uri: Uri): Boolean {
    return GOOGLE_PHOTOS_URI == uri.authority
}

private fun isGoogleDriveUri(uri: Uri): Boolean {
    return uri.authority!!.contains(GOOGLE_DOCS_URI)
}

fun getFilePath(context: Context, uri: Uri): String? {
    return if (isKitkatSupportDevice) {
        getFilePathFromUriForNewAPI(context, uri)
    } else {
        getFilePathFromUriForOldAPI(context, uri)
    }
}

private fun getFilePathFromUriForOldAPI(context: Context, uri: Uri): String? {
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    var result: String? = null
    val cursorLoader = CursorLoader(context, uri, projection, null, null, null)
    val cursor = cursorLoader.loadInBackground()

    if (cursor != null) {
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        result = cursor.getString(columnIndex)
        cursor.close()
    }
    return result
}

@SuppressLint("NewApi")
private fun getFilePathFromUriForNewAPI(context: Context, uri: Uri): String? {
    if (isKitkatSupportDevice && DocumentsContract.isDocumentUri(context, uri)) {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val type = split[0]

        if (isExtStorageDocument(uri)) {
            if ("primary".equals(type, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            }
        } else if (isDownloadsDocument(uri)) {
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)
                cursor!!.moveToNext()
                val fileName = cursor.getString(0)
                val path = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName
                if (!TextUtils.isEmpty(path)) {
                    return path
                }
            } finally {
                cursor?.close()
            }
            val id = DocumentsContract.getDocumentId(uri)
            if (id.startsWith("raw:")) {
                return id.replaceFirst("raw:".toRegex(), "")
            }
            val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads"), java.lang.Long.valueOf(id))

            return getDataColumn(context, contentUri, null, null)
        } else if (isMediaDocument(uri)) {
            var contentUri: Uri? = null
            when (type) {
                "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val selection = "_id=?"
            val selectionArgs = arrayOf(split[1])
            return getDataColumn(context, contentUri, selection, selectionArgs)
        }
    } else if (isGoogleDriveUri(uri)) {
        val file = loadFileFromGoogleDocs(uri, context)
        return file?.path
    } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {
        return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(context, uri, null, null)
    } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
        return uri.path
    }

    if (DocumentsContract.isDocumentUri(context, uri) && isGoogleDriveUri(uri)) {
        val driveFile = loadFileFromGoogleDocs(uri, context)
        return driveFile?.path
    } else {
        return null
    }
}

private fun loadFileFromGoogleDocs(uri: Uri, context: Context): File? {
    var driveFile: File? = null
    val cursor = App.getInstance().contentResolver.query(uri, null, null, null, null)
    if (cursor != null) {
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()

        val name = cursor.getString(nameIndex)
        driveFile = File(context.cacheDir, name)

        val input = context.contentResolver.openInputStream(uri)
        val output = FileOutputStream(driveFile)
        try {
            input.use {
                output.use {
                    input?.copyTo(output)
                }
            }
        } catch (e: Exception) {
            Log.d("ImageUtils", e.message)
        } finally {
            cursor.close()
        }
    }
    return driveFile
}

private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(column)

    try {
        cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(index)
        }
    } finally {
        cursor?.close()
    }
    return null
}

private fun getAppExternalDataDirectoryFile(): File {
    val dataDirectoryFile = File(getAppExternalDataDirectoryPath())
    dataDirectoryFile.mkdirs()
    return dataDirectoryFile
}

private fun getAppExternalDataDirectoryPath(): String {
    val sb = StringBuilder()
    sb.append(Environment.getExternalStorageDirectory())
            .append(File.separator)
            .append("Android")
            .append(File.separator)
            .append("data")
            .append(File.separator)
            .append(App.getInstance().packageName)
            .append(File.separator)
    return sb.toString()
}

fun startFilePicker(fragment: Fragment) {
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.type = FILES_MIME
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    fragment.startActivityForResult(Intent.createChooser(intent, "Select File"), FILE_REQUEST_CODE)
}

fun startMediaPicker(fragment: Fragment) {
    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
    intent.type = VIDEO_OR_IMAGE_MIME

    // TODO Files: Delete further single string to add sending all file types
    intent.type = IMAGE_MIME
    //

    fragment.startActivityForResult(Intent.createChooser(intent, fragment.getString(R.string.dlg_choose_file_from)), GALLERY_REQUEST_CODE)
}

fun startCameraForResult(fragment: Fragment) {
    val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

    // TODO Files: Uncomment to add sending all file types
    //val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)

    val chooser = Intent.createChooser(pictureIntent, "Capture Video or Photo")

    // TODO Files: Uncomment to add sending all file types
    //chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(videoIntent))

    chooser.resolveActivity(App.getInstance().packageManager)?.let {
        val photoFile = getTemporaryCameraFile()
        pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, getValidUri(photoFile, fragment.context))
        fragment.startActivityForResult(chooser, CAMERA_REQUEST_CODE)
    }
}

fun getTemporaryCameraFile(): File {
    val storageDir = getAppExternalDataDirectoryFile()
    val file = File(storageDir, getTemporaryCameraFileName())
    try {
        file.createNewFile()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return file
}

fun getLastUsedCameraFile(): File? {
    val dataDir = getAppExternalDataDirectoryFile()
    val files = dataDir.listFiles()
    val filteredFiles = ArrayList<File>()
    for (file in files) {
        if (file.name.startsWith(CAMERA_FILE_NAME_PREFIX)) {
            filteredFiles.add(file)
        }
    }
    filteredFiles.sort()
    return if (filteredFiles.isNotEmpty()) {
        filteredFiles[filteredFiles.size - 1]
    } else {
        null
    }
}

private fun getValidUri(file: File, context: Context?): Uri {
    val outputUri: Uri
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val authority = context!!.packageName + ".provider"
        outputUri = FileProvider.getUriForFile(context, authority, file)
    } else {
        outputUri = Uri.fromFile(file)
    }
    return outputUri
}

private fun getTemporaryCameraFileName(): String {
    return CAMERA_FILE_NAME_PREFIX + System.currentTimeMillis() + ".jpg"
}