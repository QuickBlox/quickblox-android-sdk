package com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.attachment

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class AttachmentAdapter(private val items: ArrayList<AttachmentModel>, private val attachmentListener: AttachmentListener) :
        RecyclerView.Adapter<AttachmentViewHolder>(), AttachmentViewHolder.AttachmentListener {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        return AttachmentViewHolder.newInstance(parent, this)
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun removeFile(attachmentModel: AttachmentModel, position: Int) {
        attachmentListener.removeFile(attachmentModel)
    }

    interface AttachmentListener {
        fun removeFile(attachmentModel: AttachmentModel)
    }
}