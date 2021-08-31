package com.husker.minecraft.launcher.app.minecraft.versions

import com.husker.minecraft.launcher.app.minecraft.blocks.Block
import com.husker.minecraft.launcher.app.minecraft.blocks.ModelBlock
import com.husker.minecraft.launcher.app.minecraft.blocks.impl.DefaultBlock
import com.husker.minecraft.launcher.app.minecraft.scene.PreviewParameters
import com.husker.minecraft.launcher.app.minecraft.versions.impl.V16
import com.husker.minecraft.launcher.app.minecraft.versions.impl.V17
import javafx.scene.image.Image
import javafx.scene.paint.Color

abstract class MineVersion {

    companion object{
        @JvmStatic var v1_16 = V16()
        @JvmStatic var v1_17 = V17()

    }

    private val blocks = hashMapOf<String, () -> Block>()

    private var versionTag: String = "v0"
    val textureSource = MultiplyTextureSource()

    fun configureVersion(versionTag: String){
        this.versionTag = versionTag
        textureSource.add(VersionTextureSource(versionTag))
    }

    open fun getLogo() : Image = Image("/minecraft/$versionTag/logo.png")
    open fun getPreviewParameters() : PreviewParameters = PreviewParameters(getPreviewColor()) {
        MineVersion::class.java.getResourceAsStream("/minecraft/$versionTag/preview.map")!!
    }

    abstract fun getPreviewColor() : Color

    fun addBlock(name: String, block: () -> Block){
        blocks[name] = block
    }

    fun getBlockInstance(name : String) : Block {
        return getBlockInstance(name, Block.Side.values().associate { it to 15 }, Block.Side.values().asList(), emptyMap())
    }

    fun getBlockInstance(name : String, lights: Map<Block.Side, Int>, sides: List<Block.Side>, blockData: Map<String, String>) : Block {
        val found = if(!blocks.containsKey(name))
            ModelBlock(this, name)
        else blocks[name]?.invoke() ?: DefaultBlock(this)

        found.sides = sides
        found.lights = lights
        found.blockData = blockData
        found.onInitialize()
        return found
    }

}