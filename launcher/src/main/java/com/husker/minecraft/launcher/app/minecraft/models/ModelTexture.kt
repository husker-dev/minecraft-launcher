package com.husker.minecraft.launcher.app.minecraft.models

import com.husker.minecraft.launcher.app.minecraft.Block
import com.husker.minecraft.launcher.tools.fx.LauncherTimer
import com.husker.minecraft.launcher.tools.fx.RectangleShape
import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.geometry.Point3D
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
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
    var source: TextureSource?,
    var light: Int?,
    point1: Point3D,
    point2: Point3D,
    point3: Point3D,
    point4: Point3D,
    private var subTexture: SubTexture = SubTexture.full
    ): RectangleShape(point1, point2, point3, point4) {

    companion object{
        var empty = ModelTexture(Block.nullTexture, 0, Point3D.ZERO, Point3D.ZERO, Point3D.ZERO, Point3D.ZERO)

        private var cachedSplittedTextures = HashMap<Image, HashMap<SubTexture, Array<Image>>>()

        private var animationTimers = HashMap<Int, Timer>()
        private var animationListeners = HashMap<Int, ArrayList<Consumer<Int>>>()

        private fun addAnimation(period: Int, event: Consumer<Int>){
            if(!animationTimers.containsKey(period)){
                var frame = 0
                animationListeners[period] = arrayListOf()

                //    Period = 1 sec / 20 ticks * speed
                //    50 * speed
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
            object: AnimationTimer(){
                override fun handle(now: Long) {
                    for(texture in texturesToShow){
                        texture.textureOpacity += 0.15
                        texture.textureOpacity = min(texture.textureOpacity, 1.0)

                        texture.updateDiffuseColor()
                    }
                    texturesToShow = texturesToShow.filter { it.textureOpacity < 1.0 }.toCollection(ArrayList())
                }
            }.start()
        }
    }

    private var textureOpacity: Double = 0.0
    private lateinit var meta: TextureMeta

    constructor(texture: Image?,
                light: Int?,
                point1: Point3D,
                point2: Point3D,
                point3: Point3D,
                point4: Point3D,
                subTexture: SubTexture = SubTexture.full):
    this("block", object: TextureSource(){
        override fun getTexture(path: String): Image = texture ?: Block.nullTexture
        override fun getMeta(path: String): TextureMeta = TextureMeta.default
        override fun getModel(path: String): String = ""
    }, light, point1, point2, point3, point4, subTexture)

    fun initialize(){
        val tex = source!!.getTexture(blockId)
        meta = source!!.getMeta(blockId)

        if(!cachedSplittedTextures.contains(tex))
            cachedSplittedTextures[tex] = HashMap()

        if(!cachedSplittedTextures[tex]!!.containsKey(subTexture))
            cachedSplittedTextures[tex]!![subTexture] = splitImageVertically(tex)

        val frames = cachedSplittedTextures[tex]!![subTexture]!!

        applyTexture(frames[0])
        if(frames.isNotEmpty()) {
            addAnimation(meta.frametime) {
                applyTexture(frames[it % frames.size])
            }
        }
    }

    fun startAnimation(){
        textureOpacity = 0.0
        texturesToShow.add(this)
    }

    private fun setTextureOpacity(opacity: Double){
        textureOpacity = opacity
        updateDiffuseColor()
    }

    private fun updateDiffuseColor(){
        val lightPercent = light!! / 15.0
        phongMaterial.diffuseColor = Color(lightPercent * textureOpacity, lightPercent * textureOpacity, lightPercent * textureOpacity, textureOpacity)
    }

    fun cullFace(cullFace: CullFace): ModelTexture {
        setCullFace(cullFace)
        return this
    }

    private fun applyTexture(texture: Image){
        phongMaterial.diffuseMap = texture
        updateDiffuseColor()
    }

    private fun splitImageVertically(image: Image): Array<Image>{
        val frames = arrayListOf<Image>()
        val width = (subTexture.x2 - subTexture.x1).toInt()
        val height = (subTexture.y2 - subTexture.y1).toInt()

        if(width == 0 || height == 0)
            return arrayOf(Block.nullTexture)

        for (i in 0 until (image.height / Block.size).toInt()) {
            val frame = WritableImage(width, height)
            val currentHeight: Int = Block.size * i

            for (x in 0 until width)
                for (y in 0 until height)
                    frame.pixelWriter.setColor(x, y, image.pixelReader.getColor(x + subTexture.x1.toInt(), y + subTexture.y1.toInt() + currentHeight))

            frames.add(frame)
        }

        return frames.toTypedArray()
    }

    data class SubTexture(var x1: Double, var y1: Double, var x2: Double, var y2: Double){

        companion object{
            @JvmStatic val full = SubTexture(0.0, 0.0, 16.0, 16.0)
        }

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