package com.husker.minecraft.launcher.app.minecraft.blocks.impl

import com.husker.minecraft.launcher.app.minecraft.MineVersion
import com.husker.minecraft.launcher.app.minecraft.blocks.ModelBlock

class AxisBlock(version: MineVersion, var id: String): ModelBlock(version) {

    override fun onInitialize() {
        name = if(blockData["axis"] == "z")
            "${id}_ew"
        else
            "${id}_ns"
    }
}