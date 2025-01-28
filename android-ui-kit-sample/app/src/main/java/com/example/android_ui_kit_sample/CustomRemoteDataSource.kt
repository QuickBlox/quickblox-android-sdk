/*
 * Created by Injoit on 28.1.2025.
 * Copyright © 2025 Quickblox. All rights reserved.
 *
 */

package com.example.android_ui_kit_sample

import android.util.Log
import com.quickblox.android_ui_kit.data.dto.remote.dialog.RemoteDialogDTO
import com.quickblox.android_ui_kit.data.dto.remote.user.RemoteUserDTO
import com.quickblox.android_ui_kit.data.dto.remote.user.RemoteUserPaginationDTO
import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSourceExceptionFactoryImpl
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSourceImpl
import com.quickblox.android_ui_kit.data.source.remote.mapper.RemoteDialogDTOMapper
import com.quickblox.android_ui_kit.data.source.remote.mapper.RemotePaginationDTOMapper
import com.quickblox.android_ui_kit.data.source.remote.mapper.RemoteUserDTOMapper
import com.quickblox.android_ui_kit.domain.exception.repository.MappingException
import com.quickblox.auth.session.Query
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.content.QBContent
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.XMPPException
import org.jivesoftware.smackx.muc.DiscussionHistory

// TODO: The class which overrides the RemoteDataSourceImpl and provides custom implementation
class CustomRemoteDataSource : RemoteDataSourceImpl() {
    private val exceptionFactory = RemoteDataSourceExceptionFactoryImpl()

    override fun getAllDialogs(): Flow<Result<RemoteDialogDTO>> {
        return channelFlow {
            val dialogs = mutableListOf<QBChatDialog>()
            try {
                //TODO: Here we can add the custom implementation to load dialogs from custom API and sync or add some restrictions
                val qbDialogs = loadAllQBDialogs()
                dialogs.addAll(qbDialogs)
            } catch (exception: RemoteDataSourceException) {
                val defaultErrorMessage = RemoteDataSourceException.Codes.CONNECTION_FAILED.toString()
                send(Result.failure(exceptionFactory.makeUnexpected(exception.message ?: defaultErrorMessage)))
            }

            for (qbChatDialog in dialogs) {
                val isNotActiveScope = !coroutineContext.isActive
                if (isNotActiveScope) {
                    return@channelFlow
                }

                try {
                    val dialogDTO = RemoteDialogDTOMapper.toDTOFrom(qbChatDialog, getLoggedUserId())

                    if (qbChatDialog.isPrivate) {
                        val opponentAvatarUrl = getOpponentAvatarUid(qbChatDialog)
                        dialogDTO.photo = opponentAvatarUrl
                    }

                    val needJoin = !qbChatDialog.isPrivate && !qbChatDialog.isJoined
                    if (needJoin) {
                        qbChatDialog.join(DiscussionHistory())
                    }

                    send(Result.success(dialogDTO))
                } catch (exception: RemoteDataSourceException) {
                    val defaultErrorMessage = RemoteDataSourceException.Codes.CONNECTION_FAILED.toString()
                    send(Result.failure(exceptionFactory.makeUnexpected(exception.message ?: defaultErrorMessage)))
                } catch (exception: MappingException) {
                    val errorMessage = exception.message.toString() + " dialogId: ${qbChatDialog.dialogId}"
                    send(Result.failure(exceptionFactory.makeIncorrectData(errorMessage)))
                } catch (exception: XMPPException) {
                    val errorMessage = exception.message.toString() + " dialogId: ${qbChatDialog.dialogId}"
                    send(Result.failure(exceptionFactory.makeConnectionFailed(errorMessage)))
                } catch (exception: SmackException) {
                    val errorMessage = exception.message.toString() + " dialogId: ${qbChatDialog.dialogId}"
                    send(Result.failure(exceptionFactory.makeConnectionFailed(errorMessage)))
                } catch (exception: IllegalStateException) {
                    val errorMessage = exception.message.toString() + " dialogId: ${qbChatDialog.dialogId}"
                    send(Result.failure(exceptionFactory.makeUnexpected(errorMessage)))
                }
            }
        }
    }

    private fun getOpponentAvatarUid(dialog: QBChatDialog): String? {
        var opponentAvatarUid: String? = null

        try {
            val opponentId = getOpponentIdFromPrivate(dialog)
            val opponent = loadUserById(opponentId)
            opponentAvatarUid = loadUserAvatarUid(opponent?.fileId)
        } catch (exception: RuntimeException) {
            Log.e(TAG, exception.message.toString())
        }
        return opponentAvatarUid
    }

    private fun loadUserAvatarUid(blobId: Int?): String? {
        blobId?.let { id ->
            try {
                val file = QBContent.getFile(id).perform()
                return file.uid
            } catch (exception: QBResponseException) {
                Log.e(TAG, exception.message.toString())
            }
        }
        return null
    }

    private fun loadUserById(userId: Int?): QBUser? {
        if (userId == null || userId <= 0) {
            throw RuntimeException("user Id can't be null")
        }

        try {
            return QBUsers.getUser(userId).perform()
        } catch (exception: QBResponseException) {
            throw RuntimeException("Error loading user  userId - $userId")
        }
    }

    private fun getOpponentIdFromPrivate(dialog: QBChatDialog): Int? {
        val isNotPrivate = !dialog.isPrivate
        if (isNotPrivate || dialog.occupants.isEmpty()) {
            throw RuntimeException("Dialog is not private or not contains opponents")
        }

        return dialog.occupants.find { it != getLoggedUserId() }
    }

    override fun getAllUsers(dto: RemoteUserPaginationDTO): Flow<Result<Pair<RemoteUserDTO, RemoteUserPaginationDTO>>> {
        return channelFlow {
            // TODO: Here we can add the custom implementation to load users from custom API and sync or add some restrictions
            val requestBuilder = RemotePaginationDTOMapper.pagedRequestBuilderFrom(dto)

            val rule = makeRequestRuleToExcludeUserId(getLoggedUserId())
            requestBuilder.rules = arrayListOf(rule)

            val pairResult = loadAllQBUsers(requestBuilder)
            val users = pairResult.first
            val pagination = pairResult.second

            for (user in users) {
                try {
                    val userDTO = RemoteUserDTOMapper.toDTOFrom(user)

                    userDTO.avatarUrl = loadUserAvatarUrl(userDTO.blobId)

                    send(Result.success(Pair(userDTO, pagination)))
                } catch (e: RemoteDataSourceException) {
                    send(Result.failure(e))
                } catch (exception: MappingException) {
                    send(Result.failure(exceptionFactory.makeIncorrectData(exception.message.toString() + " dialogId: ${user.id}")))
                }
            }
        }
    }

    private fun loadAllQBUsers(pageRequestBuilder: QBPagedRequestBuilder): Pair<List<QBUser>, RemoteUserPaginationDTO> {
        try {
            // TODO: Here we can add the custom implementation to load users from QuickBlox
            val performer = QBUsers.getUsers(pageRequestBuilder) as Query
            val users = performer.perform()

            val bundle = performer.bundle
            val dto = RemotePaginationDTOMapper.remoteUserPaginationDtoFrom(bundle)

            return Pair(users, dto)
        } catch (exception: QBResponseException) {
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        } catch (exception: RuntimeException) {
            throw exceptionFactory.makeRestrictedAccess(exception.message.toString())
        }
    }

    private fun loadUserAvatarUrl(blobId: Int?): String? {
        blobId?.let { id ->
            try {
                val file = QBContent.getFile(id).perform()
                return file.privateUrl
            } catch (exception: QBResponseException) {
                Log.e(TAG, exception.message.toString())
            }
        }
        return null
    }
}