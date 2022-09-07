package com.quickblox.sample.videochat.kotlin.executor

import android.os.Handler
import android.os.Looper
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

private const val THREAD_POOL_SIZE = 3
private const val MAX_POOL_SIZE = 3
private const val KEEP_ALIVE_TIME = 1L

object Executor {
    private var threadPoolExecutor: ThreadPoolExecutor? = null
    private val threadQueue: BlockingQueue<Runnable> = LinkedBlockingQueue()

    init {
        threadPoolExecutor = ThreadPoolExecutor(
            THREAD_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            threadQueue
        )
    }

    fun <T> addTask(executorTask: ExecutorTask<T>) {
        threadPoolExecutor?.execute {
            val mainHandler = Handler(Looper.getMainLooper())

            try {
                val result = executorTask.onBackground()
                mainHandler.post {
                    executorTask.onForeground(result)
                }
            } catch (exception: Exception) {
                mainHandler.post {
                    executorTask.onError(exception)
                }
            }
        }
    }
}