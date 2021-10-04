package com.quickblox.sample.conference.kotlin.domain.di

import android.content.Context
import com.quickblox.sample.conference.kotlin.data.di.FileRepositoryModule
import com.quickblox.sample.conference.kotlin.domain.files.FileManager
import com.quickblox.sample.conference.kotlin.domain.files.FileManagerImpl
import com.quickblox.sample.conference.kotlin.domain.repositories.file.FileRepository
import com.quickblox.sample.conference.kotlin.executor.Executor
import com.quickblox.sample.conference.kotlin.executor.ExecutorModule
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
@Module(includes = [FileRepositoryModule::class, ExecutorModule::class])
object FileManagerModule {
    @Provides
    @Singleton
    fun provideFilesManager(@ApplicationContext context: Context, fileRepository: FileRepository, executor: Executor): FileManager {
        return FileManagerImpl(context, fileRepository, executor)
    }
}