package com.quickblox.sample.conference.kotlin.presentation.screens.attachment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.ActivityAttachmentImageBinding
import com.quickblox.sample.conference.kotlin.databinding.PopupAttachmentBinding
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

private const val ATTACHMENT_TITLE = "attachment"
private const val EXTRA_URL = "extra_url"

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@AndroidEntryPoint
class AttachmentImageActivity : BaseActivity<AttachmentImageViewModel>(AttachmentImageViewModel::class.java) {
    private lateinit var binding: ActivityAttachmentImageBinding
    private var bitmap: Bitmap? = null

    companion object {
        fun start(context: Context, url: String) {
            val intent = Intent(context, AttachmentImageActivity::class.java)
            intent.putExtra(EXTRA_URL, url)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttachmentImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadImage()

        viewModel.liveData.observe(this, { result ->
            result?.let { (state, data) ->
                when (state) {
                    ViewState.SHOW_LOGIN_SCREEN -> {
                        LoginActivity.start(this)
                        finish()
                    }
                }
            }
        })

        setClickListeners()
    }

    private fun loadImage() {
        val urlString = intent?.getStringExtra(EXTRA_URL)
        urlString?.let {
            Glide.with(this)
                    .asBitmap()
                    .load(it)
                    .error(R.drawable.ic_error_white)
                    .listener(ImageLoadListener())
                    .dontTransform()
                    .into(binding.ivImage)
        }
    }

    private fun setClickListeners() {
        binding.flBack.setOnClickListener {
            onBackPressed()
        }

        binding.ivMore.setOnClickListener {
            val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val bindingPopUp = PopupAttachmentBinding.inflate(layoutInflater)

            val popupWindow = PopupWindow(bindingPopUp.root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            popupWindow.isOutsideTouchable = true
            popupWindow.showAsDropDown(it, 0, 0)

            bindingPopUp.tvSaveAttachment.setOnClickListener {
                Toast.makeText(applicationContext, getString(R.string.save_attachment), Toast.LENGTH_SHORT).show()
                saveFileToGallery()
                popupWindow.dismiss()
            }
        }
    }

    private fun saveFileToGallery() {
        if (bitmap != null) {
            try {
                // TODO: 6/9/21 Need to find other solution
                MediaStore.Images.Media.insertImage(contentResolver, bitmap, ATTACHMENT_TITLE, "")
                Toast.makeText(baseContext, getString(R.string.image_saved), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(baseContext, getString(R.string.unable_save), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(baseContext, getString(R.string.image_not_uploaded), Toast.LENGTH_SHORT).show()
        }
    }

    inner class ImageLoadListener : RequestListener<Bitmap> {
        init {
            binding.progressBar.visibility = View.VISIBLE
        }

        override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            bitmap = resource
            binding.progressBar.visibility = View.GONE
            return false
        }

        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
            Toast.makeText(baseContext, getString(R.string.loading_failed), Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
            return false
        }
    }

    override fun showProgress() {
        // empty
    }

    override fun hideProgress() {
        // empty
    }
}