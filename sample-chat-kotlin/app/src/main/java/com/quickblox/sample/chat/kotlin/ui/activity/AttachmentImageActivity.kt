package com.quickblox.sample.chat.kotlin.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.quickblox.sample.chat.kotlin.R
import com.quickblox.sample.chat.kotlin.utils.shortToast


private const val EXTRA_URL = "url"

class AttachmentImageActivity : BaseActivity() {

    private val TAG = AttachmentImageActivity::class.java.simpleName
    private lateinit var imageView: ImageView
    private lateinit var progressBar: ProgressBar
    private var imageLoaded = false

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            supportActionBar?.setBackgroundDrawable(getDrawable(R.drawable.toolbar_video_player_background))
        }
        supportActionBar?.elevation = 0f
        imageView = findViewById(R.id.iv_full_view)
        progressBar = findViewById(R.id.progress_show_image)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_video_player, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_player_save -> {
                saveFileToGallery()
                return true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun saveFileToGallery() {
        if (imageLoaded) {
            try {
                val bitmapToSave = imageView.drawable.toBitmap()
                MediaStore.Images.Media.insertImage(contentResolver, bitmapToSave, "attachment", "")
                shortToast("Image saved to the Gallery")
            } catch (e: Exception) {
                e.message?.let { Log.d(TAG, it) }
                shortToast("Unable to save image")
            }
        } else {
            shortToast("Image not yet downloaded")
        }
    }

    private fun loadImage() {
        val url = intent.getStringExtra(EXTRA_URL)
        progressBar.visibility = View.VISIBLE
        Glide.with(this)
                .load(url)
                .listener(DrawableListener(progressBar))
                .into(imageView)
    }

    private inner class DrawableListener(private val progressBar: ProgressBar) : RequestListener<Drawable> {
        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            progressBar.visibility = View.GONE
            imageLoaded = true
            return false
        }

        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
            var ex = e
            ex?.printStackTrace()
            if (ex?.message != null) {
                Log.d("Glide Drawable", ex.message!!)
            } else {
                ex = GlideException("Unable to load image")
            }
            showErrorSnackbar(R.string.error_load_image, ex, null)
            progressBar.visibility = View.GONE
            return false
        }
    }
}