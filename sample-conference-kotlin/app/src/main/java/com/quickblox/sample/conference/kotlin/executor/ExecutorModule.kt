package com.quickblox.sample.conference.kotlin.executor

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
@Module
object ExecutorModule {
    @Provides
    @Singleton
    fun provideExecutor(): Executor {
        return ExecutorImpl()
    }
}