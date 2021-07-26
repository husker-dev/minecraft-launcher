package com.husker.minecraft.launcher.app.minecraft.blocks

import com.husker.minecraft.launcher.app.minecraft.Block
import com.husker.minecraft.launcher.app.minecraft.MineVersion
import com.husker.minecraft.launcher.app.minecraft.models.ModelTexture
import com.husker.minecraft.launcher.app.minecraft.models.Model
import com.husker.minecraft.launcher.app.minecraft.models.TextureSource

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