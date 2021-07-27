package com.husker.minecraft.launcher.app.minecraft

import com.husker.minecraft.launcher.app.minecraft.models.ModelTexture
import javafx.geometry.Point3D
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import javafx.scene.shape.TriangleMesh

abstract class Block : Cloneable{

    enum class Side {
        Face,
        Left,
        Right,
        Top,
        Bottom,
        Back
    }

    companion object{
        val nullTexture: Image by lazy {
            val writeableImage = WritableImage(2, 2)
            val writer = writeableImage.pixelWriter
            writer.setColor(0, 0, Color.BLACK)
            writer.setColor(1, 1, Color.BLACK)
            writer.setColor(0, 1, Color.MAGENTA)
            writer.setColor(1, 0, Color.MAGENTA)
            writeableImage
        }

        const val size = 16
    }

    var renderQueuePos = 1
    var lights = HashMap<Side, Int>()
    private lateinit var cachedFilledSides: ArrayList<Side>

    private val frontTopLeft = Point3D(0.0, 1.0, 0.0)
    private val frontTopRight = Point3D(1.0, 1.0, 0.0)
    private val frontBottomLeft = Point3D(0.0, 0.0, 0.0)
    private val frontBottomRight = Point3D(1.0, 0.0, 0.0)

    private val backTopLeft = Point3D(0.0, 1.0, 1.0)
    private val backTopRight = Point3D(1.0, 1.0, 1.0)
    private val backBottomLeft = Point3D(0.0, 0.0, 1.0)
    private val backBottomRight = Point3D(1.0, 0.0, 1.0)

    protected abstract fun getTextures() : Array<ModelTexture>

    open fun getFilledSides() : Array<Side>{
        if(!this::cachedFilledSides.isInitialized){
            cachedFilledSides = arrayListOf()
            getTextures().forEach {
                if(it.point1 == backTopLeft && it.point2 == backBottomLeft && it.point3 == backTopRight && it.point4 == backBottomRight)
                    cachedFilledSides.add(Side.Back)
                if(it.point1 == frontTopLeft && it.point2 == frontBottomRight && it.point3 == frontTopLeft && it.point4 == frontBottomLeft)
                    cachedFilledSides.add(Side.Face)
                if(it.point1 == frontTopLeft && it.point2 == frontBottomLeft && it.point3 == backTopLeft && it.point4 == backBottomLeft)
                    cachedFilledSides.add(Side.Left)
                if(it.point1 == backTopRight && it.point2 == backBottomRight && it.point3 == frontTopRight && it.point4 == frontBottomRight)
                    cachedFilledSides.add(Side.Right)
                if(it.point1 == backTopRight && it.point2 == frontTopRight && it.point3 == backTopLeft && it.point4 == frontTopLeft)
                    cachedFilledSides.add(Side.Top)
                if(it.point1 == frontBottomRight && it.point2 == backBottomRight && it.point3 == frontBottomLeft && it.point4 == backBottomLeft)
                    cachedFilledSides.add(Side.Bottom)
            }
        }
        return cachedFilledSides.toTypedArray()
    }

    protected open fun filterTexture(requiredSides : ArrayList<Side>, requiredPosition: Point3D) : Array<ModelTexture>{
        if(requiredSides.size == 0)
            return emptyArray()

        var textures: List<ModelTexture> = getTextures().toCollection(ArrayList())
        textures = textures.filter {
            val type = getTextureType(it)

            if(type == Side.Face)
                if (Side.Face !in requiredSides)
                    return@filter false

            if(type == Side.Back)
                return@filter false

            if(type == Side.Top)
                if (requiredPosition.y > 5 || Side.Top !in requiredSides)
                    return@filter false

            if(type == Side.Bottom)
                if(requiredPosition.y < -2 || Side.Bottom !in requiredSides)
                    return@filter false

            if(type == Side.Left)
                if(requiredPosition.x < -2 || Side.Left !in requiredSides)
                    return@filter false

            if(type == Side.Right)
                if(requiredPosition.x > 2 || Side.Right !in requiredSides)
                    return@filter false

            return@filter true
        }

        return textures.toTypedArray()
    }

    private fun getTextureType(texture: ModelTexture): Side{
        val x = texture.point1.x
        val y = texture.point1.y
        val z = texture.point1.z

        if (texture.point2.x == x && texture.point3.x == x && texture.point4.x == x){
            return if (texture.point1.z > texture.point3.z)
                Side.Right
            else
                Side.Left
        }
        if (texture.point2.y == y && texture.point3.y == y && texture.point4.y == y){
            return if (texture.point1.z > texture.point2.z)
                Side.Top
            else
                Side.Bottom
        }
        if (texture.point2.z == z && texture.point3.z == z && texture.point4.z == z) {
            return if (texture.point1.x > texture.point3.x)
                Side.Face
            else
                Side.Back
        }

        return Side.Face
    }

    open fun getNode(requiredSides: ArrayList<Side>, requiredPosition: Point3D) : Node{
        // Mirroring Y points because scene Y is mirrored too
        return Group(filterTexture(requiredSides, requiredPosition).map {
            it.initialize()
            val points = (it.mesh as TriangleMesh).points
            for(i in 1..points.size() step 3)
                points[i] = 1 - points[i]

            return@map it
        })
    }


}