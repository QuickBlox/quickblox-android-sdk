package com.quickblox.sample.conference.kotlin.domain.settings

import com.quickblox.sample.conference.kotlin.domain.settings.entities.CallSettingsEntity

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface SettingsManager {
    fun loadCallSettings(): CallSettingsEntity
    fun applyCallSettings()
    fun applyChatSettings()
    fun saveCallSettings(callSettings: CallSettingsEntity)
    fun clearAllData()
}