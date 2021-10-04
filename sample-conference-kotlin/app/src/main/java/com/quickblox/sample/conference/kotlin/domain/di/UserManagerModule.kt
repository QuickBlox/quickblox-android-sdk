package com.quickblox.sample.conference.kotlin.domain.di

import com.quickblox.sample.conference.kotlin.data.di.DbModule
import com.quickblox.sample.conference.kotlin.data.di.SettingsRepositoryModule
import com.quickblox.sample.conference.kotlin.data.di.UserRepositoryModule
import com.quickblox.sample.conference.kotlin.domain.repositories.db.DBRepository
import com.quickblox.sample.conference.kotlin.domain.repositories.settings.SettingsRepository
import com.quickblox.sample.conference.kotlin.domain.repositories.user.UserRepository
import com.quickblox.sample.conference.kotlin.domain.user.UserManager
import com.quickblox.sample.conference.kotlin.domain.user.UserManagerImpl
import com.quickblox.sample.conference.kotlin.executor.Executor
import com.quickblox.sample.conference.kotlin.executor.ExecutorModule
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
@Module(includes = [UserRepositoryModule::class, DbModule::class, ExecutorModule::class, SettingsRepositoryModule::class])
object UserManagerModule {
    @Provides
    @Singleton
    fun provideUserManager(userRepository: UserRepository, dbRepository: DBRepository, executor: Executor,
                           settingsRepository: SettingsRepository): UserManager {
        return UserManagerImpl(userRepository, dbRepository, executor, settingsRepository)
    }
}