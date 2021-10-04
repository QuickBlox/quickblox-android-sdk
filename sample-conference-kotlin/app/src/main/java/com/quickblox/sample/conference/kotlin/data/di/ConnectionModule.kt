package com.quickblox.sample.conference.kotlin.data.di

import android.content.Context
import com.quickblox.sample.conference.kotlin.data.device.ConnectionRepositoryImpl
import com.quickblox.sample.conference.kotlin.domain.repositories.connection.ConnectionRepository
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
object ConnectionModule {
    @Provides
    @Singleton
    fun provideConnectionRepository(@ApplicationContext context: Context): ConnectionRepository {
        return ConnectionRepositoryImpl(context)
    }
}