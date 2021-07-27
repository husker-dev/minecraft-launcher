package com.husker.minecraft.launcher.app.minecraft.blocks.impl

import com.husker.minecraft.launcher.app.minecraft.MineVersion
import com.husker.minecraft.launcher.app.minecraft.blocks.ModelBlock
import com.husker.minecraft.launcher.app.minecraft.models.ModelTexture
import javafx.geometry.Point3D
import javafx.scene.shape.CullFace

open class FireBlock(version: MineVersion, id: String): ModelBlock(version) {

    init{
        name = "${id}_floor1"
    }

    override fun getFilledSides(): Array<Side> = emptyArray()

    override fun filterTexture(requiredSides: ArrayList<Side>, requiredPosition: Point3D): Array<ModelTexture> {
        return getTextures().map {
            it.cullFace(CullFace.NONE)
            return@map it
        }.toTypedArray()
    }
}