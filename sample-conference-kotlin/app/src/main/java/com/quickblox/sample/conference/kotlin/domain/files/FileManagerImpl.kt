package com.quickblox.sample.conference.kotlin.domain.files

import android.content.Context
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import com.quickblox.content.model.QBFile
import com.quickblox.core.io.IOUtils
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.domain.DomainCallback
import com.quickblox.sample.conference.kotlin.domain.LoadFileCallBack
import com.quickblox.sample.conference.kotlin.domain.repositories.file.FileRepository
import com.quickblox.sample.conference.kotlin.executor.Executor
import com.quickblox.sample.conference.kotlin.executor.ExecutorTask
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.attachment.AttachmentModel
import java.io.File
import java.io.FileOutputStream

private const val MAX_FILE_SIZE_100MB = 104857600
private const val THUMB_SIZE = 80

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class FileManagerImpl(private val context: Context, private val fileRepository: FileRepository, private val executor: Executor) : FileManager {
    private val attachments = arrayListOf<AttachmentModel>()

    override fun getAttachmentsList(): ArrayList<AttachmentModel> {
        return attachments
    }

    override fun upload(file: File, callback: LoadFileCallBack<AttachmentModel?, Exception>, progressCallback: ProgressCallback) {
        if (isNotCorrectSizeFile(file)) {
            callback.onError(Exception(context.getString(R.string.error_attachment_size)))
            return
        }
        val bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(file.path), THUMB_SIZE, THUMB_SIZE)
        val attachmentModel = AttachmentModel(null, bitmap)
        attachments.add(attachmentModel)
        callback.onCreated(attachmentModel)
        executor.addTask(object : ExecutorTask<QBFile?> {
            override fun backgroundWork(): QBFile? {
                return fileRepository.uploadSync(file) { progress ->
                    val mainHandler = Handler(Looper.getMainLooper())
                    mainHandler.post {
                        if (progress == 1 || progress % 10 == 0) {
                            attachmentModel.progress = progress
                            progressCallback.onChangeProgress()
                        }
                    }
                }
            }

            override fun foregroundResult(result: QBFile?) {
                attachmentModel.qbFile = result
                callback.onLoaded()
                file.delete()
            }

            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }

    override fun upload(uri: Uri, callback: LoadFileCallBack<AttachmentModel?, Exception>, progressCallback: ProgressCallback) {
        createFileFromUri(uri, object : DomainCallback<File?, Exception> {
            override fun onSuccess(result: File?, bundle: Bundle?) {
                result?.let { file ->
                    upload(file, callback, progressCallback)
                }
            }

            override fun onError(error: Exception) {
                callback.onError(error)
            }
        })
    }


    private fun isNotCorrectSizeFile(file: File): Boolean {
        return if (file.length() > MAX_FILE_SIZE_100MB) {
            file.delete()
            true
        } else {
            false
        }
    }

    override fun delete(attachment: AttachmentModel, callback: DomainCallback<Void?, Exception>) {
        executor.addTask(object : ExecutorTask<Void?> {
            override fun backgroundWork(): Void? {
                return attachment.qbFile?.id?.let { fileRepository.deleteSync(it) }
            }

            override fun foregroundResult(result: Void?) {
                attachments.remove(attachment)
                callback.onSuccess(result, null)
            }

            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }

    private fun createFileFromUri(uri: Uri, callback: DomainCallback<File?, Exception>) {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.let {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            val name = it.getString(nameIndex)
            val file = File(context.cacheDir, name)
            val input = context.contentResolver.openInputStream(uri)
            val output = FileOutputStream(file)
            executor.addTask(object : ExecutorTask<File?> {
                override fun backgroundWork(): File? {
                    if (input == null) {
                        return null
                    }
                    IOUtils.copy(input, output)
                    input.close()
                    output.close()
                    return file
                }

                override fun foregroundResult(result: File?) {
                    it.close()
                    callback.onSuccess(result, null)
                }

                override fun onError(exception: Exception) {
                    it.close()
                    callback.onError(exception)
                }
            })
        }
    }
}