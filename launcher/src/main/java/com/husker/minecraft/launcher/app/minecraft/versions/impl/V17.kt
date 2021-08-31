package com.husker.minecraft.launcher.app.minecraft.versions.impl

import javafx.scene.paint.Color

class V17: V16() {

    init{
        configureVersion("v17")
    }

    override fun getPreviewColor(): Color = Color.web("#87B3FF")
}