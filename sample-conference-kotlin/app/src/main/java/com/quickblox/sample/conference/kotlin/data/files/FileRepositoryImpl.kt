package com.quickblox.sample.conference.kotlin.data.files

import android.os.Bundle
import com.quickblox.content.QBContent
import com.quickblox.content.model.QBFile
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.QBProgressCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.conference.kotlin.data.DataCallBack
import com.quickblox.sample.conference.kotlin.domain.repositories.file.FileRepository
import java.io.File

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class FileRepositoryImpl : FileRepository {
    @Throws(Exception::class)
    override fun uploadSync(file: File, progressCallback: QBProgressCallback): QBFile? {
        val publicAccess = false
        val tags = null
        return QBContent.uploadFileTask(file, publicAccess, tags, progressCallback).perform()
    }

    override fun uploadAsync(file: File, progressCallback: QBProgressCallback, callback: DataCallBack<QBFile?, Exception>) {
        val publicAccess = false
        val tags = null
        QBContent.uploadFileTask(file, publicAccess, tags, progressCallback).performAsync(object : QBEntityCallback<QBFile> {
            override fun onSuccess(qbFile: QBFile?, boundel: Bundle?) {
                callback.onSuccess(qbFile, boundel)
            }

            override fun onError(exception: QBResponseException) {
                callback.onError(exception)
            }
        })
    }

    @Throws(Exception::class)
    override fun deleteSync(fileId: Int): Void? {
        return QBContent.deleteFile(fileId).perform()
    }

    override fun deleteAsync(fileId: Int, callback: DataCallBack<Void?, Exception>) {
        QBContent.deleteFile(fileId).performAsync(object : QBEntityCallback<Void?> {
            override fun onSuccess(void: Void?, bundle: Bundle?) {
                callback.onSuccess(void, bundle)
            }

            override fun onError(exception: QBResponseException) {
                callback.onError(exception)
            }
        })
    }
}