package com.husker.minecraft.launcher.app.minecraft.versions

import com.husker.minecraft.launcher.app.Resources
import com.husker.minecraft.launcher.app.minecraft.model.TextureMeta
import com.husker.minecraft.launcher.app.minecraft.model.TextureSource
import javafx.scene.image.Image
import java.io.InputStream

open class VersionTextureSource(var versionTag: String): TextureSource(){
    private val cachedTextures = HashMap<String, Image>()
    private val cachedModels = HashMap<String, String>()
    private val cachedMeta = HashMap<String, TextureMeta>()

    override fun getTexture(path: String): InputStream {
        val fullPath = "/minecraft/${versionTag}/textures/$path.png"

        return VersionTextureSource::class.java.getResourceAsStream(fullPath)!!
    }

    override fun getModel(path: String): String? {
        val fullPath = "/minecraft/${versionTag}/models/$path.json"

        if(VersionTextureSource::class.java.getResource(fullPath) == null)
            return null

        if(!cachedModels.containsKey(path))
            cachedModels[path] = Resources.text(fullPath)
        return cachedModels[path]!!
    }

    override fun getMeta(path: String): TextureMeta? {
        val fullPath = "/minecraft/${versionTag}/textures/$path.png.mcmeta"

        if(VersionTextureSource::class.java.getResource(fullPath) == null)
            return null

        if(!cachedMeta.containsKey(path))
            cachedMeta[path] = TextureMeta(Resources.text(fullPath))
        return cachedMeta[path]!!
    }
}