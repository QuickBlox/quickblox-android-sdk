package com.quickblox.sample.conference.kotlin.executor

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface ExecutorTask<T> {
    @Throws(Exception::class)
    fun backgroundWork(): T

    fun foregroundResult(result: T)

    fun onError(exception: Exception)
}