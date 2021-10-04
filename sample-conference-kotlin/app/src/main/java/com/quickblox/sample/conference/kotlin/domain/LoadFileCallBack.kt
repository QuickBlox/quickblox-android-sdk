package com.quickblox.sample.conference.kotlin.domain

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */interface LoadFileCallBack<T, E> {
    fun onLoaded()
    fun onCreated(result: T)
    fun onError(error: E)
}