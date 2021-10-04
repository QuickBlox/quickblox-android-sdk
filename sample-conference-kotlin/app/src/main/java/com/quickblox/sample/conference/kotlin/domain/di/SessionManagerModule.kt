package com.quickblox.sample.conference.kotlin.domain.di

import com.quickblox.sample.conference.kotlin.data.di.SessionRepositoryModule
import com.quickblox.sample.conference.kotlin.domain.repositories.session.SessionRepository
import com.quickblox.sample.conference.kotlin.domain.session.SessionManager
import com.quickblox.sample.conference.kotlin.domain.session.SessionManagerImpl
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
@Module(includes = [SessionRepositoryModule::class])
object SessionManagerModule {
    @Provides
    @Singleton
    fun provideSessionManager(sessionRepository: SessionRepository): SessionManager {
        return SessionManagerImpl(sessionRepository)
    }
}