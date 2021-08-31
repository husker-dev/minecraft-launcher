package com.husker.minecraft.launcher.app.minecraft.model

import java.io.InputStream

abstract class TextureSource {

    abstract fun getTexture(path: String): InputStream?
    abstract fun getMeta(path: String): TextureMeta?
    abstract fun getModel(path: String): String?
}