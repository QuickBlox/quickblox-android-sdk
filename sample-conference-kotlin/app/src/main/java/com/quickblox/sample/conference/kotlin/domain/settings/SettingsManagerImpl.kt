package com.quickblox.sample.conference.kotlin.domain.settings

import com.quickblox.sample.conference.kotlin.domain.repositories.settings.SettingsRepository
import com.quickblox.sample.conference.kotlin.domain.settings.entities.CallSettingsEntity

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class SettingsManagerImpl(private val settingsRepository: SettingsRepository) : SettingsManager {
    override fun loadCallSettings(): CallSettingsEntity {
        return settingsRepository.loadCallSettings()
    }

    override fun saveCallSettings(callSettings: CallSettingsEntity) {
        settingsRepository.saveCallSettings(callSettings)
    }

    override fun clearAllData() {
        settingsRepository.clearSettings()
    }

    override fun applyCallSettings() {
        settingsRepository.applyCallSettings()
    }

    override fun applyChatSettings() {
        settingsRepository.applyChatSettings()
    }
}