package com.husker.minecraft.launcher.app.minecraft.versions

import com.husker.minecraft.launcher.app.minecraft.blocks.Block
import com.husker.minecraft.launcher.app.minecraft.model.TextureMeta
import javafx.scene.image.Image
import java.io.InputStream

open class MultiplyTextureSource: VersionTextureSource(""){

    private val sources = arrayListOf<VersionTextureSource>()

    fun add(source: VersionTextureSource){
        sources.add(source)
    }

    override fun getTexture(path: String): InputStream {
        for(source in sources){
            val found = source.getTexture(path)
            if(found != null)
                return found
        }
        return null!!
    }

    override fun getModel(path: String): String {
        for(source in sources){
            val found = source.getModel(path)
            if(found != null)
                return found
        }
        return "null"
    }

    override fun getMeta(path: String): TextureMeta {
        for(source in sources){
            val found = source.getMeta(path)
            if(found != null)
                return found
        }
        return TextureMeta.default
    }
}