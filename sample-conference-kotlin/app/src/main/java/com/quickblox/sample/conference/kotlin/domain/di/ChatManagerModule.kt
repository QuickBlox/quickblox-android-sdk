package com.quickblox.sample.conference.kotlin.domain.di

import com.quickblox.sample.conference.kotlin.data.di.ChatRepositoryModule
import com.quickblox.sample.conference.kotlin.data.di.DbModule
import com.quickblox.sample.conference.kotlin.data.di.DialogRepositoryModule
import com.quickblox.sample.conference.kotlin.data.di.SettingsRepositoryModule
import com.quickblox.sample.conference.kotlin.domain.chat.ChatManager
import com.quickblox.sample.conference.kotlin.domain.chat.ChatManagerImpl
import com.quickblox.sample.conference.kotlin.domain.repositories.chat.ChatRepository
import com.quickblox.sample.conference.kotlin.domain.repositories.db.DBRepository
import com.quickblox.sample.conference.kotlin.domain.repositories.dialog.DialogRepository
import com.quickblox.sample.conference.kotlin.domain.repositories.settings.SettingsRepository
import com.quickblox.sample.conference.kotlin.executor.Executor
import com.quickblox.sample.conference.kotlin.executor.ExecutorModule
import com.quickblox.sample.conference.kotlin.presentation.resources.ResourcesManager
import com.quickblox.sample.conference.kotlin.presentation.resources.ResourcesManagerModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@InstallIn(SingletonComponent::class)
@Module(includes = [DbModule::class, DialogRepositoryModule::class, ChatRepositoryModule::class, ResourcesManagerModule::class,
    ExecutorModule::class, SettingsRepositoryModule::class])
object ChatManagerModule {
    @Provides
    @Singleton
    fun provideChatManager(dbRepository: DBRepository, dialogRepository: DialogRepository, chatRepository: ChatRepository,
                           resourcesManager: ResourcesManager, executor: Executor, settingsRepository: SettingsRepository): ChatManager {
        return ChatManagerImpl(dbRepository, dialogRepository, chatRepository, resourcesManager, executor, settingsRepository)
    }
}