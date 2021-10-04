package com.quickblox.sample.conference.kotlin.domain.di

import com.quickblox.sample.conference.kotlin.data.di.NotificationRepositoryModule
import com.quickblox.sample.conference.kotlin.data.di.PushRepositoryModule
import com.quickblox.sample.conference.kotlin.domain.push.PushManager
import com.quickblox.sample.conference.kotlin.domain.push.PushManagerImpl
import com.quickblox.sample.conference.kotlin.domain.repositories.notification.NotificationRepository
import com.quickblox.sample.conference.kotlin.domain.repositories.push.PushRepository
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
@Module(includes = [PushRepositoryModule::class, NotificationRepositoryModule::class])
object PushManagerModule {
    @Provides
    @Singleton
    fun providePushManager(pushRepository: PushRepository, notificationRepository: NotificationRepository): PushManager {
        return PushManagerImpl(pushRepository, notificationRepository)
    }
}