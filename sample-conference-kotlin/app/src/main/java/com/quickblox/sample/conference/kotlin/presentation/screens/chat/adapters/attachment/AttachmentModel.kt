package com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.attachment

import android.graphics.Bitmap
import com.quickblox.content.model.QBFile

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
data class AttachmentModel(var qbFile: QBFile? = null, val bitmap: Bitmap? = null, var progress: Int = 0)