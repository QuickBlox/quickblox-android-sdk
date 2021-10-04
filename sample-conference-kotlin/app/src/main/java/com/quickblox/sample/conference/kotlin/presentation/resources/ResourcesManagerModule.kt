package com.quickblox.sample.conference.kotlin.presentation.resources

import android.content.Context
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
@Module
object ResourcesManagerModule {
    @Provides
    @Singleton
    fun provideResourcesManager(@ApplicationContext context: Context): ResourcesManager {
        return ResourcesManagerImpl(context)
    }
}