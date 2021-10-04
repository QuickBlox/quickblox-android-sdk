package com.quickblox.sample.conference.kotlin.domain.user

import android.os.Bundle
import com.quickblox.chat.QBChatService
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.sample.conference.kotlin.domain.DomainCallback
import com.quickblox.sample.conference.kotlin.domain.repositories.db.DBRepository
import com.quickblox.sample.conference.kotlin.domain.repositories.settings.SettingsRepository
import com.quickblox.sample.conference.kotlin.domain.repositories.user.UserRepository
import com.quickblox.sample.conference.kotlin.executor.Executor
import com.quickblox.sample.conference.kotlin.executor.ExecutorTask
import com.quickblox.users.model.QBUser

const val USER_DEFAULT_PASSWORD = "quickblox"
const val UNAUTHORIZED_CODE = 401
const val NOT_FOUND_CODE = 404

class UserManagerImpl(private val userRepository: UserRepository, private val dbRepository: DBRepository,
                      private val executor: Executor, private val settingsRepository: SettingsRepository) : UserManager {
    override fun signIn(login: String, fullName: String, callback: DomainCallback<QBUser, Exception>) {
        val qbUser = QBUser()
        qbUser.login = login
        qbUser.fullName = fullName
        qbUser.password = USER_DEFAULT_PASSWORD

        executor.addTask(object : ExecutorTask<QBUser> {
            override fun backgroundWork(): QBUser {

                val user = userRepository.signInSync(qbUser)

                return if (user.fullName != null && user.fullName.equals(qbUser.fullName)) {
                    user
                } else {
                    //Need to set password NULL, because server will update user only with NULL password
                    qbUser.password = null
                    userRepository.updateSync(qbUser)
                }
            }

            override fun foregroundResult(result: QBUser) {
                dbRepository.saveUser(result)
                callback.onSuccess(result, null)
            }

            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }

    override fun signUp(login: String, fullName: String, callback: DomainCallback<QBUser?, Exception>) {
        dbRepository.removeUser()
        val qbUser = QBUser()
        qbUser.login = login
        qbUser.fullName = fullName
        qbUser.password = USER_DEFAULT_PASSWORD

        executor.addTask(object : ExecutorTask<QBUser> {
            override fun backgroundWork(): QBUser {
                return userRepository.signUpSync(qbUser)
            }

            override fun foregroundResult(result: QBUser) {
                callback.onSuccess(result, null)
            }

            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }

    override fun signOut(callback: DomainCallback<Unit?, Exception>) {
        executor.addTask(object : ExecutorTask<Pair<Void, Bundle>> {
            override fun backgroundWork(): Pair<Void, Bundle> {
                return userRepository.signOutSync()
            }

            override fun foregroundResult(result: Pair<Void, Bundle>) {
                QBChatService.getInstance().destroy()
                dbRepository.clearAllData()
                settingsRepository.clearSettings()
                callback.onSuccess(Unit, result.second)
            }

            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }

    override fun isSavedUser(): Boolean {
        return dbRepository.getCurrentUser() != null
    }

    override fun getCurrentUser(): QBUser? {
        return dbRepository.getCurrentUser()
    }

    override fun clearUser() {
        dbRepository.removeUser()
    }

    override fun loadUsers(requestBuilder: QBPagedRequestBuilder, callback: DomainCallback<ArrayList<QBUser>, Exception>) {
        executor.addTask(object : ExecutorTask<ArrayList<QBUser>> {
            override fun backgroundWork(): ArrayList<QBUser> {
                return userRepository.loadSync(requestBuilder)
            }

            override fun foregroundResult(result: ArrayList<QBUser>) {
                callback.onSuccess(result, null)
            }

            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }

    override fun loadUsersByQuery(query: String, requestBuilder: QBPagedRequestBuilder, callback: DomainCallback<ArrayList<QBUser>, Exception>) {
        executor.addTask(object : ExecutorTask<ArrayList<QBUser>> {
            override fun backgroundWork(): ArrayList<QBUser> {
                return userRepository.loadByQuerySync(query, requestBuilder)
            }

            override fun foregroundResult(result: ArrayList<QBUser>) {
                callback.onSuccess(result, null)
            }

            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }

    override fun loadUsersByIds(userIds: Collection<Int>, callback: DomainCallback<ArrayList<QBUser>, Exception>) {
        executor.addTask(object : ExecutorTask<ArrayList<QBUser>> {
            override fun backgroundWork(): ArrayList<QBUser> {
                return userRepository.loadByIdsSync(userIds)
            }

            override fun foregroundResult(result: ArrayList<QBUser>) {
                callback.onSuccess(result, null)
            }

            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }

    override fun loadUserById(userId: Int, callback: DomainCallback<QBUser, Exception>) {
        executor.addTask(object : ExecutorTask<QBUser> {
            override fun backgroundWork(): QBUser {
                return userRepository.loadByIdSync(userId)
            }

            override fun foregroundResult(result: QBUser) {
                callback.onSuccess(result, null)
            }

            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }
}