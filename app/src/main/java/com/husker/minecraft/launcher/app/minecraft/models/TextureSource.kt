package com.husker.minecraft.launcher.app.minecraft.models

import javafx.scene.image.Image

abstract class TextureSource {

    abstract fun getTexture(path: String): Image
    abstract fun getMeta(path: String): TextureMeta
    abstract fun getModel(path: String): String
}