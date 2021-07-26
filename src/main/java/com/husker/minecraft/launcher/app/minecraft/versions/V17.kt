package com.husker.minecraft.launcher.app.minecraft.versions

import javafx.scene.paint.Color

class V17: V16() {

    init{
        versionTag = "v17"
    }

    override fun getPreviewColor(): Color = Color.web("#87B3FF")
}