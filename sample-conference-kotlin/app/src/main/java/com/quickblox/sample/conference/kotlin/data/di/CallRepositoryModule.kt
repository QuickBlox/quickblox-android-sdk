package com.quickblox.sample.conference.kotlin.data.di

import android.content.Context
import com.quickblox.sample.conference.kotlin.data.call.CallRepositoryImpl
import com.quickblox.sample.conference.kotlin.domain.repositories.call.CallRepository
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
object CallRepositoryModule {
    @Provides
    @Singleton
    fun provideCallRepository(@ApplicationContext context: Context): CallRepository {
        return CallRepositoryImpl(context)
    }
}