package com.husker.minecraft.launcher.app.minecraft.blocks.impl

import com.husker.minecraft.launcher.app.minecraft.Block
import com.husker.minecraft.launcher.app.minecraft.models.ModelTexture

class AirBlock: Block() {

    override fun getFilledSides(): Array<Side> = emptyArray()
    override fun getTextures(): Array<ModelTexture> = emptyArray()

}