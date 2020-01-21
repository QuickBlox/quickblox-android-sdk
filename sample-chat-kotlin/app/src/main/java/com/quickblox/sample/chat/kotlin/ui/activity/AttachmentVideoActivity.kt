package com.quickblox.sample.chat.kotlin.ui.activity

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.VideoView
import com.quickblox.sample.chat.kotlin.R
import java.io.File


private const val EXTRA_FILE_NAME = "video_file_name"
private const val EXTRA_FILE_URL = "video_file_URL"

class AttachmentVideoActivity : BaseActivity() {

    private lateinit var rootLayout: RelativeLayout
    private lateinit var videoView: VideoView
    private lateinit var progressBar: ProgressBar
    private lateinit var mediaController: MediaController
    private var file: File? = null

    companion object {
        fun start(context: Context, attachmentName: String?, url: String?) {
            val intent = Intent(context, AttachmentVideoActivity::class.java)
            intent.putExtra(EXTRA_FILE_URL, url)
            intent.putExtra(EXTRA_FILE_NAME, attachmentName)
            context.startActivity(intent)
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_video)
        initUI()
        loadVideo()
    }

    private fun initUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            supportActionBar?.setBackgroundDrawable(getDrawable(R.drawable.toolbar_video_player_background))
        }
        supportActionBar?.title = intent.getStringExtra(EXTRA_FILE_NAME)
        supportActionBar?.elevation = 0f
        rootLayout = findViewById(R.id.layout_root)
        videoView = findViewById(R.id.vv_full_view)
        progressBar = findViewById(R.id.progress_show_video)

        rootLayout.setOnClickListener {
            mediaController.show(2000)
        }
    }

    private fun loadVideo() {
        progressBar.visibility = View.VISIBLE
        val fileName = intent.getStringExtra(EXTRA_FILE_NAME)
        file = File(application.filesDir, fileName)

        file?.let {
            mediaController = MediaController(this)
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)
            videoView.setVideoPath(it.path)
            videoView.start()
        }
        videoView.setOnPreparedListener {
            progressBar.visibility = View.GONE
            mediaController.show(2000)
        }

        videoView.setOnErrorListener(object : MediaPlayer.OnErrorListener {
            override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                progressBar.visibility = View.GONE
                mediaController.hide()
                showErrorSnackbar(R.string.error_load_video, null, object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        loadVideo()
                    }
                })
                return true
            }
        })
    }

    override fun onBackPressed() {
        finish()
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
        if (file != null) {
            try {
                val url = intent.getStringExtra(EXTRA_FILE_URL)
                val request = DownloadManager.Request(Uri.parse(url))
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file!!.name)
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.allowScanningByMediaScanner()
                val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                manager.enqueue(request)
            } catch (e : SecurityException) {
                Log.d("Security Exception", e.message)
            }
        }
    }
}