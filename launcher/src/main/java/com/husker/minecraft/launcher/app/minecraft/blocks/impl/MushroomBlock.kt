package com.husker.minecraft.launcher.app.minecraft.blocks.impl

import com.husker.minecraft.launcher.app.minecraft.blocks.Block
import com.husker.minecraft.launcher.app.minecraft.model.ModelTexture

open class MushroomBlock(version: String, name: String): Block() {

    //private var texture: Image = loadTexture(version, name)
    override fun getTextures(): Array<ModelTexture> {
        TODO("Not yet implemented")
    }

    /*
    override fun getFilledSides(): Array<Side> = emptyArray()

    override fun getTextures(lights: HashMap<Side, Int>, requiredPosition: Point3D): Array<ModelTexture> {
        val maxLight = lights.maxOf { it.value }

        return arrayOf(
            ModelTexture(texture, maxLight, frontTopRight, frontBottomRight, backTopLeft, backBottomLeft),
            ModelTexture(texture, maxLight, backTopRight, backBottomRight, frontTopLeft, frontBottomLeft)
        )
    }

     */
}