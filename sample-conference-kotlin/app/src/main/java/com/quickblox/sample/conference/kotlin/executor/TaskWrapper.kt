package com.quickblox.sample.conference.kotlin.executor

import java.util.concurrent.Future

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
data class TaskWrapper(var executorTask: ExecutorTask<*>?, var future: Future<*>?) {
    fun clearTask() {
        future?.cancel(true)
        executorTask = null
    }
}