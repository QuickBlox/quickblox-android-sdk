/*
 * Created by Injoit on 28.1.2025.
 * Copyright © 2025 Quickblox. All rights reserved.
 *
 */

package com.example.android_ui_kit_sample

import android.content.Context
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSource
import com.quickblox.android_ui_kit.dependency.DataSourceFactory
import com.quickblox.android_ui_kit.dependency.DataSourceFactoryImpl
import com.quickblox.android_ui_kit.dependency.DependencyImpl
import com.quickblox.android_ui_kit.dependency.RepositoryFactory
import com.quickblox.android_ui_kit.dependency.RepositoryFactoryImpl
import com.quickblox.android_ui_kit.domain.repository.AIRepository
import com.quickblox.android_ui_kit.domain.repository.ConnectionRepository
import com.quickblox.android_ui_kit.domain.repository.DialogsRepository
import com.quickblox.android_ui_kit.domain.repository.EventsRepository
import com.quickblox.android_ui_kit.domain.repository.FilesRepository
import com.quickblox.android_ui_kit.domain.repository.MessagesRepository
import com.quickblox.android_ui_kit.domain.repository.UsersRepository

class CustomDependency(context: Context) : DependencyImpl(context) {
    private val dataSourceFactory: DataSourceFactory = DataSourceFactoryImpl(context)

    // TODO: Here we are replacing the RemoteDataSource with CustomRemoteDataSource
    private val customRemoteDataSource: RemoteDataSource = CustomRemoteDataSource()

    private val repositoryFactory: RepositoryFactory = RepositoryFactoryImpl(
        customRemoteDataSource,
        dataSourceFactory.createLocal(),
        dataSourceFactory.createLocalFile(),
        dataSourceFactory.createAi()
    )

    private val connectionRepository = repositoryFactory.createConnection()
    private val dialogsRepository = repositoryFactory.createDialogs()
    private val filesRepository = repositoryFactory.createFiles()
    private val messageRepository = repositoryFactory.createMessages()
    private val usersRepository = repositoryFactory.createUsers()
    private val eventsRepository = repositoryFactory.createEvents()
    private val aiRepository = repositoryFactory.createAI()

    override fun getConnectionRepository(): ConnectionRepository {
        return connectionRepository
    }

    override fun getDialogsRepository(): DialogsRepository {
        return dialogsRepository
    }

    override fun getFilesRepository(): FilesRepository {
        return filesRepository
    }

    override fun getMessagesRepository(): MessagesRepository {
        return messageRepository
    }

    override fun getUsersRepository(): UsersRepository {
        return usersRepository
    }

    override fun getEventsRepository(): EventsRepository {
        return eventsRepository
    }

    override fun getAIRepository(): AIRepository {
        return aiRepository
    }
}