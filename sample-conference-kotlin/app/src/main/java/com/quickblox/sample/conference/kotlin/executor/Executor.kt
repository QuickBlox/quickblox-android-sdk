package com.quickblox.sample.conference.kotlin.executor

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface Executor {
    @Throws(Exception::class)
    fun <T> addTask(executorTask: ExecutorTask<T>)

    @Throws(Exception::class)
    fun <T> addTaskWithKey(executorTask: ExecutorTask<T>?, key: String)

    fun removeTask(key: String)
}