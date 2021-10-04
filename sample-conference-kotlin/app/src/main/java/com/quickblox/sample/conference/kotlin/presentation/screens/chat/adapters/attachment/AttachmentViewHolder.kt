package com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.attachment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.sample.conference.kotlin.databinding.ItemAttachmentPreviewBinding

private const val MAX_PROGRESS_100 = 100

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class AttachmentViewHolder(private val binding: ItemAttachmentPreviewBinding, private val attachmentListener: AttachmentListener) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun newInstance(parent: ViewGroup, attachmentListener: AttachmentListener): AttachmentViewHolder {
            return AttachmentViewHolder(ItemAttachmentPreviewBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false), attachmentListener)
        }
    }

    fun bind(attachmentModel: AttachmentModel, position: Int) {
        binding.ivAttachment.setImageBitmap(attachmentModel.bitmap)
        binding.progressBar.progress = attachmentModel.progress

        if (attachmentModel.progress == MAX_PROGRESS_100) {
            binding.progressBar.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.VISIBLE
        }
        binding.ivRemove.setOnClickListener {
            attachmentListener.removeFile(attachmentModel, position)
        }
    }

    interface AttachmentListener {
        fun removeFile(attachmentModel: AttachmentModel, position: Int)
    }
}