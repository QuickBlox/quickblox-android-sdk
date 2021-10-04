package com.quickblox.sample.conference.kotlin.domain.di

import com.quickblox.sample.conference.kotlin.data.di.SettingsRepositoryModule
import com.quickblox.sample.conference.kotlin.domain.repositories.settings.SettingsRepository
import com.quickblox.sample.conference.kotlin.domain.settings.SettingsManager
import com.quickblox.sample.conference.kotlin.domain.settings.SettingsManagerImpl
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
@Module(includes = [SettingsRepositoryModule::class])
object SettingsManagerModule {
    @Provides
    @Singleton
    fun provideSettingsManager(settingsRepository: SettingsRepository): SettingsManager {
        return SettingsManagerImpl(settingsRepository)
    }
}