package com.quickblox.sample.conference.kotlin.data.di

import android.content.Context
import com.quickblox.sample.conference.kotlin.data.device.NotificationRepositoryImpl
import com.quickblox.sample.conference.kotlin.domain.repositories.notification.NotificationRepository
import com.quickblox.sample.conference.kotlin.presentation.resources.ResourcesManager
import com.quickblox.sample.conference.kotlin.presentation.resources.ResourcesManagerModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@InstallIn(SingletonComponent::class)
@Module(includes = [ResourcesManagerModule::class])
object NotificationRepositoryModule {
    @Provides
    @Singleton
    fun provideNotificationRepository(@ApplicationContext context: Context, resourcesManager: ResourcesManager): NotificationRepository {
        return NotificationRepositoryImpl(context, resourcesManager)
    }
}