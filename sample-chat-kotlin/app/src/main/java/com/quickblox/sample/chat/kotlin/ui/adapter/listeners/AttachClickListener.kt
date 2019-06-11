package com.quickblox.sample.chat.kotlin.ui.adapter.listeners

import com.quickblox.chat.model.QBAttachment


interface AttachClickListener {
    fun onLinkClicked(attachment: QBAttachment, positionInAdapter: Int)
}