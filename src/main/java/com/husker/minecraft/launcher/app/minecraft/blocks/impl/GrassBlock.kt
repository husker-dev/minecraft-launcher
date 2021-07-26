package com.husker.minecraft.launcher.app.minecraft.blocks.impl

import com.husker.minecraft.launcher.app.minecraft.MineVersion
import com.husker.minecraft.launcher.app.minecraft.blocks.ModelBlock
import com.husker.minecraft.launcher.app.minecraft.models.ModelTexture
import javafx.geometry.Point3D
import javafx.scene.shape.CullFace
import javafx.scene.transform.Translate
import java.util.*
import kotlin.math.max

class GrassBlock(version: MineVersion, name: String): ModelBlock(version, name) {

    override fun getTextures(): Array<ModelTexture> {
        val actualLight = max(0, lights.values.maxOf { it } - 1)

        val xt = (Random().nextDouble() * 2 - 1) / 5
        val zt = (Random().nextDouble() * 2 - 1) / 5

        return super.getTextures().map {
            it.light = actualLight
            it.cullFace = CullFace.NONE
            it.transforms.add(Translate(xt, 0.0, zt))
            return@map it
        }.toTypedArray()
    }


}