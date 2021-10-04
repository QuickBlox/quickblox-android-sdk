package com.quickblox.sample.conference.kotlin.presentation.resources

import android.content.Context
import android.content.res.Resources

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class ResourcesManagerImpl(val context: Context) : ResourcesManager {
    override fun get(): Resources {
        return context.resources
    }
}