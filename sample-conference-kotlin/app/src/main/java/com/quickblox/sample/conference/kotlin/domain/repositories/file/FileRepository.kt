package com.quickblox.sample.conference.kotlin.domain.repositories.file

import com.quickblox.content.model.QBFile
import com.quickblox.core.QBProgressCallback
import com.quickblox.sample.conference.kotlin.data.DataCallBack
import java.io.File

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface FileRepository {
    fun uploadSync(file: File, progressCallback: QBProgressCallback): QBFile?
    fun uploadAsync(file: File, progressCallback: QBProgressCallback, callback: DataCallBack<QBFile?, Exception>)

    fun deleteSync(fileId: Int): Void?
    fun deleteAsync(fileId: Int, callback: DataCallBack<Void?, Exception>)
}