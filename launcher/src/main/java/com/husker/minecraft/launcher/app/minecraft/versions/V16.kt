package com.husker.minecraft.launcher.app.minecraft.versions

import com.husker.minecraft.launcher.app.Resources
import com.husker.minecraft.launcher.app.minecraft.MineVersion
import com.husker.minecraft.launcher.app.minecraft.blocks.impl.AirBlock
import com.husker.minecraft.launcher.app.minecraft.blocks.impl.FireBlock
import com.husker.minecraft.launcher.app.minecraft.blocks.impl.GrassBlock
import com.husker.minecraft.launcher.app.minecraft.scene.PreviewParameters
import javafx.scene.paint.Color

open class V16 : MineVersion() {

    init{
        versionTag = "v16"

        addBlock("void_air") { AirBlock() }

        // Fire
        addBlock("fire") { FireBlock(this, "fire") }
        addBlock("soul_fire") { FireBlock(this, "soul_fire") }

        // Fungus
        addBlock("warped_fungus") { GrassBlock(this, "warped_fungus") }
        addBlock("warped_fungus") { GrassBlock(this, "warped_fungus") }

        // Roots
        addBlock("crimson_roots") { GrassBlock(this, "crimson_roots") }
        addBlock("warped_roots") { GrassBlock(this, "warped_roots") }

        // Sprouds
        addBlock("nether_sprouts") { GrassBlock(this, "nether_sprouts") }

    }

    override fun getPreviewColor(): Color = Color.web("#380E0C")

    /*
    override fun getPreviewParameters(): PreviewParameters {
        return PreviewParameters(v1_17.getPreviewColor()) { Resources.json("/minecraft/v17/preview.json") }
    }

     */
}