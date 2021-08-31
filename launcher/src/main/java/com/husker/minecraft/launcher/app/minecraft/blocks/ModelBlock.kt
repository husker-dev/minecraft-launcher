package com.husker.minecraft.launcher.app.minecraft.blocks

import com.husker.minecraft.launcher.app.minecraft.versions.MineVersion
import com.husker.minecraft.launcher.app.minecraft.model.ModelTexture
import com.husker.minecraft.launcher.app.minecraft.model.Model
import com.husker.minecraft.launcher.app.minecraft.model.TextureSource

open class ModelBlock(var source: TextureSource, var name: String = "block"): Block() {

    private val model: Model by lazy {
        Model.get(name, source)
    }

    constructor(version: MineVersion, name: String = "block"): this(version.textureSource, name)

    override fun getTextures(): Array<ModelTexture> {
        return try {
            model.getTextures(lights)
        }catch (e: Exception){
            emptyArray()
        }
    }

}