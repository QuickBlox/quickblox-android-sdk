package com.quickblox.sample.conference.kotlin.domain.files

import android.net.Uri
import com.quickblox.sample.conference.kotlin.domain.DomainCallback
import com.quickblox.sample.conference.kotlin.domain.LoadFileCallBack
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.attachment.AttachmentModel
import java.io.File

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface FileManager {
    fun getAttachmentsList(): ArrayList<AttachmentModel>
    fun upload(file: File, callback: LoadFileCallBack<AttachmentModel?, Exception>, progressCallback: ProgressCallback)
    fun upload(uri: Uri, callback: LoadFileCallBack<AttachmentModel?, Exception>, progressCallback: ProgressCallback)
    fun delete(attachment: AttachmentModel, callback: DomainCallback<Void?, Exception>)
}