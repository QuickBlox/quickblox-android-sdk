package com.quickblox.sample.conference.kotlin.domain.di

import android.content.Context
import com.quickblox.sample.conference.kotlin.data.di.CallRepositoryModule
import com.quickblox.sample.conference.kotlin.domain.call.CallManager
import com.quickblox.sample.conference.kotlin.domain.call.CallManagerImpl
import com.quickblox.sample.conference.kotlin.domain.repositories.call.CallRepository
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
@Module(includes = [ResourcesManagerModule::class, CallRepositoryModule::class])
object CallManagerModule {
    @Provides
    @Singleton
    fun provideCallManager(@ApplicationContext context: Context, resourcesManager: ResourcesManager, callRepository: CallRepository): CallManager {
        return CallManagerImpl(context, resourcesManager, callRepository)
    }
}