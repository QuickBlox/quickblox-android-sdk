package com.quickblox.sample.conference.kotlin.presentation.screens.chat

import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.RECORD_AUDIO
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider.getUriForFile
import androidx.recyclerview.widget.LinearLayoutManager
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.data.service.CallService
import com.quickblox.sample.conference.kotlin.databinding.ActivityChatBinding
import com.quickblox.sample.conference.kotlin.databinding.PopupChatLayoutBinding
import com.quickblox.sample.conference.kotlin.domain.chat.PROPERTY_CONVERSATION_ID
import com.quickblox.sample.conference.kotlin.presentation.screens.attachment.AttachmentImageActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.call.CallActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.attachment.AttachmentAdapter
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.attachment.AttachmentModel
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.message.ChatAdapter
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.message.ChatMessage
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.message.HeaderDecoration
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.message.PROPERTY_NOTIFICATION_TYPE
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.message.START_CONFERENCE
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.message.START_STREAM
import com.quickblox.sample.conference.kotlin.presentation.screens.chatinfo.ChatInfoActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.login.LoginActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.main.MainActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.main.POPUP_MAIN_WIDTH
import com.quickblox.sample.conference.kotlin.presentation.utils.Constants.EXTRA_DIALOG_ID
import com.quickblox.sample.conference.kotlin.presentation.utils.convertToPx
import com.quickblox.sample.conference.kotlin.presentation.utils.setOnClick
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

private const val CAMERA_PERMISSION_CODE = 999
private const val MEDIA_PERMISSIONS_CODE = 111
private const val STORAGE_PERMISSIONS_CODE = 222
private const val START_CONFERENCE_PERMISSION_CODE = 888
private const val JOIN_CONFERENCE_PERMISSION_CODE = 777
private const val START_STREAM_PERMISSION_CODE = 666
private const val JOIN_STREAM_PERMISSION_CODE = 555
private const val POSITION_GALLERY = 0
private const val POSITION_CAMERA = 1
private const val MAX_ATTACHMENTS_COUNT = 1
private const val IMAGE_MIME = "image/*"

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@AndroidEntryPoint
class ChatActivity : BaseActivity<ChatViewModel>(ChatViewModel::class.java), EasyPermissions.PermissionCallbacks {
    private lateinit var binding: ActivityChatBinding
    private var bindingPopUp: PopupChatLayoutBinding? = null
    private var chatAdapter: ChatAdapter? = null
    private var attachmentAdapter: AttachmentAdapter? = null
    private var content: ActivityResultLauncher<String>? = null
    private var cameraImage: ActivityResultLauncher<Uri>? = null
    private var file: File? = null
    private var chatMessage: ChatMessage? = null

    companion object {
        fun start(context: Context, dialogId: String) {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(EXTRA_DIALOG_ID, dialogId)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityResult()

        binding = ActivityChatBinding.inflate(layoutInflater)
        val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        bindingPopUp = PopupChatLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initAttachmentAdapter()
        initChatAdapter()
        setClickListeners()
        viewModel.liveData.observe(this) { result ->
            result?.let { (state, data) ->
                when (state) {
                    ViewState.PROGRESS -> {
                        showProgress()
                    }

                    ViewState.ERROR -> {
                        hideProgress()
                        Toast.makeText(baseContext, "$data", Toast.LENGTH_SHORT).show()
                    }

                    ViewState.ERROR_LOAD_ATTACHMENT -> {
                        hideProgress()
                        Toast.makeText(baseContext, "$data", Toast.LENGTH_SHORT).show()
                        binding.ivSend.isEnabled = true
                    }

                    ViewState.ERROR_UPLOAD -> {
                        hideProgress()
                        Toast.makeText(baseContext, "$data", Toast.LENGTH_SHORT).show()
                    }

                    ViewState.RECEIVED_MESSAGE -> {
                        val updateToolbar = data as Boolean
                        if (updateToolbar) {
                            fillToolBar()
                        }
                        chatAdapter?.notifyItemInserted(viewModel.messages.size - 1)
                        scrollMessageListDown(viewModel.messages)
                    }

                    ViewState.LOADER_PROGRESS_UPDATED -> {
                        attachmentAdapter?.notifyDataSetChanged()
                    }

                    ViewState.MESSAGES_SHOWED -> {
                        hideProgress()
                        val size = data as Int
                        if (viewModel.skipPagination == 0) {
                            scrollMessageListDown(viewModel.messages)
                        }
                        chatAdapter?.notifyItemRangeInserted(0, size)
                    }

                    ViewState.MESSAGE_SENT -> {
                        checkAttachmentSize()
                        binding.etMessage.setText("")
                    }

                    ViewState.SHOW_ATTACHMENT_SCREEN -> {
                        val url = data as String
                        AttachmentImageActivity.start(this@ChatActivity, url)
                    }

                    ViewState.LEAVE -> {
                        finish()
                    }

                    ViewState.FILE_SHOWED -> {
                        hideProgress()
                        binding.ivSend.isEnabled = false
                        attachmentAdapter?.notifyDataSetChanged()
                        checkAttachmentSize()
                    }

                    ViewState.FILE_LOADED -> {
                        Toast.makeText(baseContext, getString(R.string.attachment_loaded), Toast.LENGTH_SHORT).show()
                        binding.ivSend.isEnabled = true
                    }

                    ViewState.FILE_DELETED -> {
                        checkAttachmentSize()
                    }

                    ViewState.SHOW_CALL_SCREEN -> {
                        viewModel.currentDialog?.dialogId?.let { CallActivity.start(this@ChatActivity) }
                        viewModel.liveData.clearValue()

                        if (CallService.isRunning()) {
                            finish()
                        }
                        hideProgress()
                    }

                    ViewState.UPDATE_TOOLBAR -> {
                        fillToolBar()
                    }

                    ViewState.SHOW_LOGIN_SCREEN -> {
                        LoginActivity.start(this)
                        finish()
                    }

                    ViewState.SHOW_INFO_SCREEN -> {
                        viewModel.currentDialog?.dialogId?.let { dialogId ->
                            ChatInfoActivity.start(this, dialogId)
                        }
                    }
                }
            }
        }
        val dialogId = intent.getStringExtra(EXTRA_DIALOG_ID)
        dialogId?.let {
            viewModel.loadDialogById(it)
            fillToolBar()
        } ?: run {
            Toast.makeText(baseContext, getString(R.string.dialogId_error), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun activityResult() {
        content = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { viewModel.uploadFile(it) }
        }

        cameraImage = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                file?.let { viewModel.uploadFile(it) }
            }
        }
    }

    private fun checkAttachmentSize() {
        if (viewModel.getAttachments().size < 1) {
            binding.llAttachment.visibility = View.GONE
        } else {
            binding.llAttachment.visibility = View.VISIBLE
        }
    }

    private fun initAttachmentAdapter() {
        attachmentAdapter =
            AttachmentAdapter(viewModel.getAttachments(), object : AttachmentAdapter.AttachmentListener {
                override fun removeFile(attachmentModel: AttachmentModel) {
                    viewModel.removeQBFile(attachmentModel)
                    binding.ivSend.isEnabled = true
                }
            })
        binding.rvPreview.adapter = attachmentAdapter
    }

    private fun initChatAdapter() {
        chatAdapter = viewModel.currentUser?.let {
            ChatAdapter(viewModel.messages, it, viewModel.getUsersDialog(), object : ChatAdapter.ChatAdapterListener {
                override fun readMessage(qbChatMessage: QBChatMessage?) {
                    qbChatMessage?.let { viewModel.readMessage(it) }
                }

                override fun onClickAttachment(url: String) {
                    viewModel.showAttachmentScreen(url)
                }

                override fun onClickJoin(chatMessage: ChatMessage) {
                    showProgress()
                    this@ChatActivity.chatMessage = chatMessage
                    val notificationType = getNotificationTypeFromChatMessage(chatMessage)
                    if (EasyPermissions.hasPermissions(this@ChatActivity, CAMERA, RECORD_AUDIO)) {
                        joinToCall(notificationType)
                    } else {
                        requestPermission(notificationType)
                    }
                }
            })
        }
        chatAdapter?.setPaginationHistoryListener(object : ChatAdapter.PaginationListener {
            override fun onNextPage() {
                viewModel.loadMessages()
            }
        })

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true

        binding.rvMessages.layoutManager = layoutManager
        binding.rvMessages.adapter = chatAdapter
        binding.rvMessages.itemAnimator = null

        chatAdapter?.let { HeaderDecoration(it) }?.let { binding.rvMessages.addItemDecoration(it) }
    }

    private fun requestPermission(notificationType: String) {
        when (notificationType) {
            START_CONFERENCE -> {
                requestPermissionByCode(JOIN_CONFERENCE_PERMISSION_CODE)
            }

            START_STREAM -> {
                requestPermissionByCode(JOIN_STREAM_PERMISSION_CODE)
            }
        }
    }

    private fun joinToCall(notificationType: String) {
        val conversationId = getConversationIdFromChatMessage()
        when (notificationType) {
            START_CONFERENCE -> {
                viewModel.checkExistSessionAndJoinConference(conversationId)
            }

            START_STREAM -> {
                val senderId = getSenderIdFromChatMessage()
                viewModel.checkExistSessionAndJoinStream(senderId, conversationId)
            }
        }
    }

    private fun getSenderIdFromChatMessage(): Int? {
        return chatMessage?.qbChatMessage?.senderId
    }

    private fun getConversationIdFromChatMessage(): String {
        return chatMessage?.qbChatMessage?.getProperty(PROPERTY_CONVERSATION_ID).toString()
    }

    private fun getNotificationTypeFromChatMessage(chatMessage: ChatMessage): String {
        return chatMessage.qbChatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE).toString()
    }

    private fun setClickListeners() {
        binding.flBack.setOnClickListener {
            onBackPressed()
        }

        binding.ivMore.setOnClickListener {
            val popupWindow =
                PopupWindow(bindingPopUp?.root, POPUP_MAIN_WIDTH.convertToPx(), ViewGroup.LayoutParams.WRAP_CONTENT)
            popupWindow.isOutsideTouchable = true
            popupWindow.showAsDropDown(it)

            if (CallService.isRunning()) {
                bindingPopUp?.tvStartConference?.visibility = View.GONE
                bindingPopUp?.tvStartStream?.visibility = View.GONE
                bindingPopUp?.tvLeave?.visibility = View.GONE
            }

            bindingPopUp?.tvStartConference?.setOnClickListener {
                showProgress()
                if (!EasyPermissions.hasPermissions(this, CAMERA, RECORD_AUDIO)) {
                    requestPermissionByCode(START_CONFERENCE_PERMISSION_CODE)
                } else {
                    viewModel.startConference()
                }
                popupWindow.dismiss()
            }
            bindingPopUp?.tvStartStream?.setOnClickListener {
                showProgress()
                if (!EasyPermissions.hasPermissions(this, CAMERA, RECORD_AUDIO)) {
                    requestPermissionByCode(START_STREAM_PERMISSION_CODE)
                } else {
                    viewModel.startStream()
                }
                popupWindow.dismiss()
            }
            bindingPopUp?.tvChatInfo?.setOnClickListener {
                viewModel.showChatInfoScreen()
                popupWindow.dismiss()
            }
            bindingPopUp?.tvLeave?.setOnClickListener {
                openDialogLeave()
                popupWindow.dismiss()
            }
        }

        binding.ivSend.setOnClick {
            viewModel.sendMessage(binding.etMessage.text.toString())
        }

        binding.ivAttachment.setOnClickListener {
            if (viewModel.getAttachments().size >= MAX_ATTACHMENTS_COUNT) {
                Toast.makeText(binding.root.context, R.string.error_attachment_count, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.dlg_choose_file_from)
            builder.setItems(R.array.dlg_image_pick) { _, which ->
                when (which) {
                    POSITION_GALLERY -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
                            checkAndRequestMediaPermissions()
                        } else {
                            checkAndRequestStoragePermission()
                        }
                    }

                    POSITION_CAMERA -> {
                        if (EasyPermissions.hasPermissions(this, CAMERA)) {
                            getUriFromCamera()
                        } else {
                            requestCameraPermission()
                        }
                    }
                }
            }
            builder.show()
        }
    }

    private fun checkAndRequestStoragePermission() {
        if (EasyPermissions.hasPermissions(this, READ_EXTERNAL_STORAGE)) {
            content?.launch(IMAGE_MIME)
        } else {
            requestStoragePermission()
        }
    }

    private fun checkAndRequestMediaPermissions() {
        if (EasyPermissions.hasPermissions(this, READ_MEDIA_IMAGES, READ_MEDIA_VIDEO)) {
            content?.launch(IMAGE_MIME)
        } else {
            requestMediaPermission()
        }
    }

    override fun onBackPressed() {
        if (isTaskRoot) {
            MainActivity.start(this@ChatActivity)
            finish()
        } else {
            if (CallService.isRunning()) {
                CallActivity.start(this)
                finish()
            } else {
                super.onBackPressed()
            }
            viewModel.unsubscribe()
        }
    }

    private fun requestPermissionByCode(code: Int) {
        EasyPermissions.requestPermissions(
            host = this,
            rationale = getString(R.string.conference_permissions),
            requestCode = code,
            perms = arrayOf(CAMERA, RECORD_AUDIO)
        )
    }

    private fun requestCameraPermission() {
        EasyPermissions.requestPermissions(
            host = this,
            rationale = getString(R.string.camera_permission),
            requestCode = CAMERA_PERMISSION_CODE,
            perms = arrayOf(CAMERA)
        )
    }

    private fun requestMediaPermission() {
        EasyPermissions.requestPermissions(
            host = this,
            rationale = getString(R.string.storage_permission),
            requestCode = MEDIA_PERMISSIONS_CODE,
            perms = arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO)
        )
    }

    private fun requestStoragePermission() {
        EasyPermissions.requestPermissions(
            host = this,
            rationale = getString(R.string.storage_permission),
            requestCode = STORAGE_PERMISSIONS_CODE,
            perms = arrayOf(READ_EXTERNAL_STORAGE)
        )
    }

    private fun fillToolBar() {
        viewModel.currentDialog?.let { dialog ->
            binding.toolbarTitle.text = dialog.name
            if (dialog.occupants?.size ?: 1 > 1) {
                binding.tvSubTitle.text = getString(R.string.chat_subtitle, dialog.occupants?.size.toString())
            } else {
                binding.tvSubTitle.text = getString(R.string.chat_subtitle_singular)
            }
            if (dialog.occupants.size > 12) {
                bindingPopUp?.tvStartConference?.visibility = View.GONE
            } else {
                bindingPopUp?.tvStartConference?.visibility = View.VISIBLE
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(this).build().show()
        }
        hideProgress()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        hideProgress()
        when (requestCode) {
            START_CONFERENCE_PERMISSION_CODE -> {
                if (EasyPermissions.hasPermissions(this@ChatActivity, CAMERA, RECORD_AUDIO)) {
                    viewModel.startConference()
                } else {
                    Toast.makeText(baseContext, getString(R.string.conference_permissions), Toast.LENGTH_SHORT).show()
                }
            }

            MEDIA_PERMISSIONS_CODE -> {
                if (EasyPermissions.hasPermissions(this, READ_MEDIA_IMAGES, READ_MEDIA_VIDEO)) {
                    content?.launch(IMAGE_MIME)
                } else {
                    Toast.makeText(baseContext, getString(R.string.storage_permission), Toast.LENGTH_SHORT).show()
                }
            }

            STORAGE_PERMISSIONS_CODE -> {
                if (EasyPermissions.hasPermissions(this, READ_EXTERNAL_STORAGE)) {
                    content?.launch(IMAGE_MIME)
                } else {
                    Toast.makeText(baseContext, getString(R.string.storage_permission), Toast.LENGTH_SHORT).show()
                }
            }

            CAMERA_PERMISSION_CODE -> {
                if (EasyPermissions.hasPermissions(this, CAMERA)) {
                    getUriFromCamera()
                } else {
                    Toast.makeText(baseContext, getString(R.string.camera_permission), Toast.LENGTH_SHORT).show()
                }
            }

            JOIN_CONFERENCE_PERMISSION_CODE -> {
                if (EasyPermissions.hasPermissions(this@ChatActivity, CAMERA, RECORD_AUDIO)) {
                    val conversationId = getConversationIdFromChatMessage()
                    viewModel.checkExistSessionAndJoinConference(conversationId)
                } else {
                    Toast.makeText(baseContext, getString(R.string.conference_permissions), Toast.LENGTH_SHORT).show()
                }
            }

            START_STREAM_PERMISSION_CODE -> {
                if (EasyPermissions.hasPermissions(this@ChatActivity, CAMERA, RECORD_AUDIO)) {
                    viewModel.startStream()
                } else {
                    Toast.makeText(baseContext, getString(R.string.conference_permissions), Toast.LENGTH_SHORT).show()
                }
            }

            JOIN_STREAM_PERMISSION_CODE -> {
                if (EasyPermissions.hasPermissions(this@ChatActivity, CAMERA, RECORD_AUDIO)) {
                    val conversationId = getConversationIdFromChatMessage()
                    val senderId = getSenderIdFromChatMessage()
                    viewModel.checkExistSessionAndJoinStream(senderId, conversationId)
                } else {
                    Toast.makeText(baseContext, getString(R.string.conference_permissions), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getUriFromCamera() {
        file = File(baseContext.cacheDir, viewModel.getTemporaryCameraFileName())
        var cameraImageUri: Uri? = null
        file?.let {
            cameraImageUri = getUriForFile(this, applicationContext.packageName + ".provider", it)
        }
        cameraImageUri?.let {
            cameraImage?.launch(it)
        }
    }

    private fun scrollMessageListDown(messages: ArrayList<ChatMessage>) {
        binding.rvMessages.scrollToPosition(messages.size - 1)
    }

    private fun openDialogLeave() {
        val alertDialogBuilder = AlertDialog.Builder(this@ChatActivity, R.style.AlertDialogStyle)
        alertDialogBuilder.setTitle(getString(R.string.dlg_leave_dialog))
        alertDialogBuilder.setMessage(getString(R.string.dlg_leave_question))
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setPositiveButton(getString(R.string.delete)) { _, _ -> viewModel.leaveGroupChat() }
        alertDialogBuilder.setNegativeButton(getString(R.string.cancel)) { _, _ -> }
        alertDialogBuilder.create()
        alertDialogBuilder.show()
    }

    override fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }
}