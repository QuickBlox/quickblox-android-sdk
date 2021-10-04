package com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.message

import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.quickblox.chat.model.QBAttachment
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.content.model.QBFile
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.ItemAttachLeftMessageBinding
import com.quickblox.sample.conference.kotlin.databinding.ItemInfoBinding
import com.quickblox.sample.conference.kotlin.presentation.utils.AvatarUtils
import com.quickblox.sample.conference.kotlin.presentation.utils.getTime
import com.quickblox.users.model.QBUser
import java.util.*

private const val ATTACHMENT_CORNER_RADIUS = 20f

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class AttachLeftViewHolder(private val binding: ItemAttachLeftMessageBinding, private val attachmentClickListener: AttachmentClickListener) : RecyclerView.ViewHolder(binding.root) {
    private var itemInfoBinding: ItemInfoBinding? = null

    companion object {
        fun newInstance(parent: ViewGroup, attachmentClickListener: AttachmentClickListener): AttachLeftViewHolder {
            return AttachLeftViewHolder(ItemAttachLeftMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false), attachmentClickListener)
        }
    }

    fun bind(qbChatMessage: ChatMessage, qbUser: QBUser?) {
        itemInfoBinding = ItemInfoBinding.bind(binding.root)
        itemInfoBinding?.tvName?.text = qbUser?.fullName
        itemInfoBinding?.tvTime?.text = qbChatMessage.qbChatMessage.dateSent.getTime()
        displayAttachment(qbChatMessage)
        binding.ivAvatar.setImageDrawable(AvatarUtils.getDrawableAvatar(binding.root.context, qbChatMessage.qbChatMessage.senderId.hashCode()))
        binding.tvAvatar.text = qbUser?.fullName?.replace(" ", "")?.substring(0, 1)?.toUpperCase(Locale.getDefault())
    }

    private fun displayAttachment(chatMessage: ChatMessage) {
        val attachment = getAttachment(chatMessage.qbChatMessage)
        val photoType = QBAttachment.PHOTO_TYPE.equals(attachment?.type, ignoreCase = true)
        val imageType = QBAttachment.IMAGE_TYPE.equals(attachment?.type, ignoreCase = true)
        if (photoType || imageType) {
            val imageUrl = QBFile.getPrivateUrlForUID(attachment?.id)
            Glide.with(binding.root.context)
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.attachment_image_placeholder_right)
                    .listener(ImageLoadListener())
                    .into(binding.ivAttach)

            makeRoundedCorners(binding.ivAttach, false)
            binding.root.setOnClickListener {
                attachmentClickListener.onClick(imageUrl)
            }
        } else {
            Toast.makeText(binding.root.context, binding.root.context.getString(R.string.unknown_attachment), Toast.LENGTH_SHORT).show()
        }
    }

    private fun makeRoundedCorners(imageView: ImageView?, onlyTopCorners: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && imageView != null) {
            imageView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    if (onlyTopCorners) {
                        outline.setRoundRect(0, 0, view.width, (view.height + ATTACHMENT_CORNER_RADIUS) as Int, ATTACHMENT_CORNER_RADIUS)
                    } else {
                        outline.setRoundRect(0, 0, view.width, view.height, ATTACHMENT_CORNER_RADIUS)
                    }
                }
            }
            imageView.clipToOutline = true
        }
    }

    private fun getAttachment(chatMessage: QBChatMessage): QBAttachment? {
        return chatMessage.attachments.toList()[0]
    }

    inner class ImageLoadListener : RequestListener<Drawable> {
        init {
            binding.progressBar.visibility = View.VISIBLE
        }

        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            binding.progressBar.visibility = View.GONE
            return false
        }

        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
            binding.progressBar.visibility = View.GONE
            return false
        }
    }
}