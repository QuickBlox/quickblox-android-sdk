package com.quickblox.sample.chat.kotlin.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.quickblox.sample.chat.kotlin.R
import com.quickblox.sample.chat.kotlin.utils.PREFERRED_IMAGE_SIZE_FULL

private const val EXTRA_URL = "url"

class AttachmentImageActivity : BaseActivity() {

    private lateinit var imageView: ImageView
    private lateinit var progressBar: ProgressBar

    companion object {
        fun start(context: Context, url: String) {
            val intent = Intent(context, AttachmentImageActivity::class.java)
            intent.putExtra(EXTRA_URL, url)
            context.startActivity(intent)
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_image)
        initUI()
        loadImage()
    }

    private fun initUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        imageView = findViewById(R.id.image_full_view)
        progressBar = findViewById(R.id.progress_bar_show_image)
    }

    private fun loadImage() {
        val url = intent.getStringExtra(EXTRA_URL)
        if (TextUtils.isEmpty(url)) {
            imageView.setImageResource(R.drawable.ic_error_white)
        } else {
            progressBar.visibility = View.VISIBLE
            Glide.with(this)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(DrawableListener(progressBar))
                    .error(R.drawable.ic_error_white)
                    .dontTransform()
                    .override(PREFERRED_IMAGE_SIZE_FULL, PREFERRED_IMAGE_SIZE_FULL)
                    .into(imageView)
        }
    }

    private inner class DrawableListener(private val progressBar: ProgressBar) : RequestListener<String, GlideDrawable> {

        override fun onException(e: Exception?, model: String, target: Target<GlideDrawable>,
                                 isFirstResource: Boolean): Boolean {
            e?.printStackTrace()
            showErrorSnackbar(R.string.error_load_image, null, null)
            progressBar.visibility = View.GONE
            return false
        }

        override fun onResourceReady(resource: GlideDrawable, model: String, target: Target<GlideDrawable>,
                                     isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
            progressBar.visibility = View.GONE
            return false
        }
    }
}