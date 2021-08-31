package com.husker.minecraft.launcher.app.minecraft.model

import com.husker.minecraft.launcher.app.opengl.GLTexture
import com.husker.minecraft.launcher.app.opengl.Point3D
import com.husker.minecraft.launcher.tools.fx.LauncherTimer
import com.jogamp.opengl.util.texture.Texture
import com.jogamp.opengl.util.texture.TextureCoords
import com.jogamp.opengl.util.texture.TextureIO.PNG
import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.scene.paint.Color
import javafx.scene.shape.CullFace
import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min


class ModelTexture(
    var blockId: String,
    var source: TextureSource,
    var light: Int,
    point1: Point3D,
    point2: Point3D,
    point3: Point3D,
    point4: Point3D,
    var subTexture: SubTexture = SubTexture.full
    ): GLTexture(point1, point2, point3, point4){

    companion object{
        private var cachedTextures = hashMapOf<String, Texture>()

        private var animationTimers = HashMap<Int, Timer>()
        private var animationListeners = HashMap<Int, ArrayList<Consumer<Int>>>()

        private fun addAnimation(period: Int, event: Consumer<Int>){
            if(!animationTimers.containsKey(period)){
                var frame = 0
                animationListeners[period] = arrayListOf()
                animationTimers[period] = LauncherTimer.create(0, 50 * period.toLong()){
                    frame++
                    Platform.runLater {
                        for(i in animationListeners[period]!!.indices) {
                            try {
                                animationListeners[period]!![i].accept(frame)
                            }catch (e: Exception){
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
            animationListeners[period]!!.add(event)
        }

        var texturesToShow = arrayListOf<ModelTexture>()

        init{
            /*
            object: AnimationTimer(){
                override fun handle(now: Long) {
                    for(texture in texturesToShow){
                        texture.textureOpacity += 0.15
                        texture.textureOpacity = min(texture.textureOpacity, 1.0)

                        texture.updateColor()
                    }
                    texturesToShow = texturesToShow.filter { it.textureOpacity < 1.0 }.toCollection(ArrayList())
                }
            }.start()

             */
        }
    }

    private var textureOpacity: Double = 1.0
    private var framesCoords = arrayListOf<TextureCoords>()
    private lateinit var meta: TextureMeta

    fun initialize(){
        if(blockId !in cachedTextures)
            cachedTextures[blockId] = loadTexture(source.getTexture(blockId)!!, true, PNG)

        texture = cachedTextures[blockId]!!

        meta = source.getMeta(blockId)!!

        // Cutting texture on 16x16 parts
        for(i in 0 until texture.height / 16){
            val y = i * 16
            framesCoords.add(0, texture.getSubImageTexCoords(subTexture.x1, subTexture.y1 + y, subTexture.x2, subTexture.y2 + y))
        }

        setFrame(0)

        if(framesCoords.size > 1) {
            addAnimation(meta.frametime) {
                setFrame(it % framesCoords.size)
            }
        }

        updateColor()
    }

    fun startAnimation(){
        textureOpacity = 0.0
        texturesToShow.add(this)
    }

    private fun setTextureOpacity(opacity: Double){
        textureOpacity = opacity
        updateColor()
    }

    private fun updateColor(){
        val allLightPercent = 1
        var blockLightPercent = light / 15.0
        //blockLightPercent = (1 - allLightPercent) + blockLightPercent * allLightPercent
        color = Color(blockLightPercent * textureOpacity, blockLightPercent * textureOpacity, blockLightPercent * textureOpacity, textureOpacity)
    }

    fun cullFace(cullFace: CullFace): ModelTexture {
        super.cullFace = cullFace
        return this
    }

    private fun setFrame(index: Int){
        coords = framesCoords[index]
    }

    data class SubTexture(var x1: Int, var y1: Int, var x2: Int, var y2: Int){
        companion object{
            @JvmStatic val full = SubTexture(0, 0, 16, 16)
        }

        constructor(x1: Double, y1: Double, x2: Double, y2: Double): this(x1.toInt(), y1.toInt(), x2.toInt(), y2.toInt())

        init{
            val x1 = min(this.x1, this.x2)
            val y1 = min(this.y1, this.y2)
            val x2 = max(this.x1, this.x2)
            val y2 = max(this.y1, this.y2)

            this.x1 = x1
            this.y1 = y1
            this.x2 = x2
            this.y2 = y2
        }
    }


}