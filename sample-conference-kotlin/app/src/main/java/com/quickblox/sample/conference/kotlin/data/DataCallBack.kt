package com.quickblox.sample.conference.kotlin.data

import android.os.Bundle

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface DataCallBack<T, E> {
    fun onSuccess(result: T, bundle: Bundle?)
    fun onError(error: E)
}