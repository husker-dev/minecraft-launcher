package com.husker.minecraft.launcher.app.scenes.settings

import com.husker.minecraft.launcher.app.scenes.Tab
import com.husker.minecraft.launcher.app.Resources

class SettingsTab : Tab("Settings", Resources.svg("settings.svg")) {

    init{
        bottom = true
    }
}