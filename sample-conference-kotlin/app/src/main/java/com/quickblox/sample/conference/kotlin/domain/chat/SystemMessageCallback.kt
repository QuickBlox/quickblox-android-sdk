package com.quickblox.sample.conference.kotlin.domain.chat

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface SystemMessageCallback<T, E> {
    fun onSuccess(result: T)
    fun onError(error: E)
}