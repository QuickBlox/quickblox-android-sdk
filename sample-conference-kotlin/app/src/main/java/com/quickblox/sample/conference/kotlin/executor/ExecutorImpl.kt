package com.quickblox.sample.conference.kotlin.executor

import android.os.Handler
import android.os.Looper
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

private const val THREAD_POOL_SIZE = 3
private const val KEEP_ALIVE_TIME = 1

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class ExecutorImpl : Executor {
    private val KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS
    private val taskMap = hashMapOf<String, TaskWrapper>()

    private var threadPoolExecutor: ThreadPoolExecutor? = null
    private val threadQueue: BlockingQueue<Runnable> = LinkedBlockingQueue()

    init {
        threadPoolExecutor = ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, KEEP_ALIVE_TIME.toLong(), KEEP_ALIVE_TIME_UNIT, threadQueue)
    }

    override fun <T> addTask(executorTask: ExecutorTask<T>) {
        threadPoolExecutor?.execute {
            val mainHandler = Handler(Looper.getMainLooper())

            try {
                val result = executorTask.backgroundWork()
                mainHandler.post {
                    executorTask.foregroundResult(result)
                }
            } catch (exception: Exception) {
                mainHandler.post {
                    executorTask.onError(exception)
                }
            }
        }
    }

    override fun <T> addTaskWithKey(executorTask: ExecutorTask<T>?, key: String) {
        val mainHandler = Handler(Looper.getMainLooper())

        if (taskMap.contains(key)) {
            mainHandler.post {
                executorTask?.onError(TaskExistException())
            }
            return
        }

        val future = threadPoolExecutor?.submit {

            try {
                val result = executorTask?.backgroundWork()

                mainHandler.post {
                    result?.let { executorTask.foregroundResult(it) }
                }
            } catch (exception: Exception) {
                mainHandler.post {
                    executorTask?.onError(exception)
                }
            }
        }

        taskMap[key] = TaskWrapper(executorTask, future)
    }

    override fun removeTask(key: String) {
        val task = taskMap[key]
        task?.clearTask()
        taskMap.remove(key)
    }
}