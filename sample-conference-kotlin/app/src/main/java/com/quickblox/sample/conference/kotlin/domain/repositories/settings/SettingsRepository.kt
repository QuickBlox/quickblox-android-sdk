package com.quickblox.sample.conference.kotlin.domain.repositories.settings

import com.quickblox.sample.conference.kotlin.domain.settings.entities.CallSettingsEntity

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface SettingsRepository {
    fun loadCallSettings(): CallSettingsEntity
    fun saveCallSettings(callSettings: CallSettingsEntity)
    fun applyCallSettings()
    fun clearSettings()
    fun applyChatSettings()
}