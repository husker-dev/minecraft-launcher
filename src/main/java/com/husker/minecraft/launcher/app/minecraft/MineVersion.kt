package com.husker.minecraft.launcher.app.minecraft

import com.husker.minecraft.launcher.app.Resources
import com.husker.minecraft.launcher.app.minecraft.blocks.ModelBlock
import com.husker.minecraft.launcher.app.minecraft.blocks.impl.DefaultBlock
import com.husker.minecraft.launcher.app.minecraft.models.TextureMeta
import com.husker.minecraft.launcher.app.minecraft.models.TextureSource
import com.husker.minecraft.launcher.app.minecraft.scene.PreviewParameters
import com.husker.minecraft.launcher.app.minecraft.versions.V16
import com.husker.minecraft.launcher.app.minecraft.versions.V17
import javafx.scene.image.Image
import javafx.scene.paint.Color

abstract class MineVersion {

    companion object{
        @JvmStatic var v1_16 = V16()
        @JvmStatic var v1_17 = V17()

    }

    private val blocks = hashMapOf<String, () -> Block>()

    lateinit var versionTag: String
    val textureSource = VersionTextureSource(this)

    open fun getLogo() : Image = Image("/minecraft/$versionTag/logo.png")
    open fun getPreviewParameters() : PreviewParameters = PreviewParameters(getPreviewColor()) {
        Resources.json("/minecraft/$versionTag/preview.json")
    }

    abstract fun getPreviewColor() : Color

    fun addBlock(name: String, block: () -> Block){
        blocks[name] = block
    }

    fun getBlockInstance(name : String) : Block {
        if(!blocks.containsKey(name))
            return ModelBlock(this, name)
        return blocks[name]?.invoke() ?: DefaultBlock(this)
    }

    class VersionTextureSource(var version: MineVersion): TextureSource(){
        private val cachedTextures = HashMap<String, Image>()
        private val cachedModels = HashMap<String, String>()
        private val cachedMeta = HashMap<String, TextureMeta>()

        override fun getTexture(path: String): Image {
            if(!cachedTextures.containsKey(path))
                cachedTextures[path] = Image("/minecraft/${version.versionTag}/textures/$path.png")
            return cachedTextures[path]!!
        }

        override fun getModel(path: String): String {
            if(!cachedModels.containsKey(path))
                cachedModels[path] = Resources.text("/minecraft/${version.versionTag}/models/$path.json")
            return cachedModels[path]!!
        }

        override fun getMeta(path: String): TextureMeta {
            if(!cachedMeta.containsKey(path)){
                val fullPath = "/minecraft/${version.versionTag}/textures/$path.png.mcmeta"
                cachedMeta[path] = if (Resources::class.java.getResource(fullPath) != null)
                    TextureMeta(Resources.text(fullPath))
                else TextureMeta.default
            }
            return cachedMeta[path]!!
        }
    }
}