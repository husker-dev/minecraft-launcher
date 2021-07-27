package com.husker.minecraft.launcher.app.minecraft.models

import com.husker.minecraft.launcher.app.minecraft.Block
import javafx.geometry.Point3D
import javafx.scene.image.Image
import javafx.scene.shape.CullFace
import javafx.scene.transform.Rotate
import javafx.scene.transform.Scale
import org.json.JSONObject

class Model(private var source: TextureSource) {

    companion object{
        var cachedModels = HashMap<String, Model>()

        @JvmStatic
        fun get(name: String, source: TextureSource): Model{
            return if(cachedModels.containsKey(name)) {
                cachedModels[name]!!.clone()
            }else
                Model(name, source)
        }
    }

    private var variables = HashMap<String, TextureVariable>()
    private var elements = ArrayList<TextureCube>()

    private val cachedTextures = arrayListOf<ModelTexture>()

    constructor(name: String, source: TextureSource): this(source){
        loadByName(name)
        cachedModels[name] = this
    }

    private fun loadByName(name: String){
        val json = JSONObject(source.getModel(clearResourcePath(name)))
        if(json.isEmpty)
            return

        if(json.has("parent")){
            val parent = get(json.getString("parent"), source)
            parent.variables.forEach {
                variables[it.key] = it.value.claim(this)
            }
            elements.addAll(parent.elements)
        }

        if(json.has("textures")){
            val textures = json.getJSONObject("textures")
            for(key in textures.keys())
                variables[key] = TextureVariable(this, key, textures.getString(key))
        }

        if(json.has("elements")){
            elements.clear()
            for(element in json.getJSONArray("elements").iterator())
                elements.add(TextureCube(element as JSONObject))
        }
    }

    // TODO: Model.clone() doesn't clone cached texture
    fun clone(): Model{
        val instance = Model(source)
        instance.variables = variables
        instance.elements = elements
        return instance
    }

    fun getTextures(lights: HashMap<Block.Side, Int>): Array<ModelTexture>{
        if(cachedTextures.size > 0)
            return cachedTextures.toTypedArray()

        elements.forEach { cube ->
            cube.faces.keys.forEach { faceKey ->
                val face = cube.faces[faceKey]!!
                val light: Int

                val points: Array<Point3D>

                when(faceKey){
                    "down" -> {
                        points = arrayOf(cube.frontBottomRight, cube.backBottomRight, cube.frontBottomLeft, cube.backBottomLeft)
                        light = lights[Block.Side.Bottom]!!
                    }
                    "up" -> {
                        points = arrayOf(cube.backTopRight, cube.frontTopRight, cube.backTopLeft, cube.frontTopLeft)
                        light = lights[Block.Side.Top]!!
                    }
                    "north" -> {
                        points = arrayOf(cube.backTopLeft, cube.backBottomLeft, cube.backTopRight, cube.backBottomRight)
                        light = lights[Block.Side.Back]!!
                    }
                    "south" -> {
                        points = arrayOf(cube.frontTopRight, cube.frontBottomRight, cube.frontTopLeft, cube.frontBottomLeft)
                        light = lights[Block.Side.Face]!!
                    }
                    "west" -> {
                        points = arrayOf(cube.frontTopLeft, cube.frontBottomLeft, cube.backTopLeft, cube.backBottomLeft)
                        light = lights[Block.Side.Left]!!
                    }
                    "east" -> {
                        points = arrayOf(cube.backTopRight, cube.backBottomRight, cube.frontTopRight, cube.frontBottomRight)
                        light = lights[Block.Side.Right]!!
                    }
                    else -> {
                        points = arrayOf(Point3D.ZERO, Point3D.ZERO, Point3D.ZERO, Point3D.ZERO)
                        light = 15
                    }
                }

                val imagePath = if(face.texture.startsWith("#"))
                    variables[face.texture.substring(1)]!!.get()
                else
                    face.texture

                val texture = ModelTexture(clearResourcePath(imagePath), source, light, points[0], points[1], points[2], points[3], subTexture = face.uv)
                if(!cube.shade)
                    texture.light = 15
                texture.cullFace(face.cullFace)

                if(cube.scale.x != 1.0)
                    texture.applyTransform(cube.scale)
                if(cube.rotation.angle != 0.0)
                    texture.applyTransform(cube.rotation)

                for(i in 0 until (face.rotation / 90.0).toInt())
                    texture.rotate90()
                cachedTextures.add(texture)
            }
        }
        //elements.clear()
        return cachedTextures.toTypedArray()
    }

    private fun clearResourcePath(path: String): String{
        var clearPath = path
        if(clearPath.contains("block/"))
            clearPath = clearPath.split("block/")[1]
        return clearPath
    }


    private class TextureCube(json: JSONObject){

        var from: Point3D
        var to: Point3D
        var faces = HashMap<String, TextureCubeFace>()
        var rotation = Rotate()
        var scale = Scale()
        var shade = true

        val frontTopLeft: Point3D
        val frontTopRight: Point3D
        val frontBottomLeft: Point3D
        val frontBottomRight: Point3D

        val backTopLeft: Point3D
        val backTopRight: Point3D
        val backBottomLeft: Point3D
        val backBottomRight: Point3D


        init {
            val fromJson = json.getJSONArray("from")
            val toJson = json.getJSONArray("to")
            val facesJson = json.getJSONObject("faces")

            if(json.has("rotation")){
                val rotationJson = json.getJSONObject("rotation")
                val originJson = rotationJson.getJSONArray("origin")
                val axis = when(rotationJson.getString("axis")){
                    "x" -> Rotate.X_AXIS
                    "y" -> Rotate.Y_AXIS
                    "z" -> Rotate.Z_AXIS
                    else -> Rotate.X_AXIS
                }
                val pivotX = originJson.getDouble(0) / 16
                val pivotY = originJson.getDouble(1) / 16
                val pivotZ = originJson.getDouble(2) / 16
                rotation = Rotate(rotationJson.getDouble("angle"), pivotX, pivotY, pivotZ, axis)
                if(rotationJson.has("rescale") && rotationJson.getBoolean("rescale")){
                    var rotationSizeMultiplier = 1.0
                    if(rotation.angle % 45.0 == 0.0)
                        rotationSizeMultiplier = 1.41421356237
                    else if(rotation.angle % 22.5 == 0.0)
                        rotationSizeMultiplier = 1.149048125
                    if(axis == Rotate.Y_AXIS)
                        scale = Scale(rotationSizeMultiplier, 1.0, rotationSizeMultiplier, pivotX, pivotY, pivotZ)
                    //if(axis == Rotate.X_AXIS)
                }
            }

            from = TexturePoint(fromJson.getDouble(0), fromJson.getDouble(1), fromJson.getDouble(2))
            to = TexturePoint(toJson.getDouble(0), toJson.getDouble(1), toJson.getDouble(2))

            frontTopLeft = Point3D(from.x, to.y, from.z)
            frontTopRight = Point3D(to.x, to.y, from.z)
            frontBottomLeft = Point3D(from.x, from.y, from.z)
            frontBottomRight = Point3D(to.x, from.y, from.z)

            backTopLeft = Point3D(from.x, to.y, to.z)
            backTopRight = Point3D(to.x, to.y, to.z)
            backBottomLeft = Point3D(from.x, from.y, to.z)
            backBottomRight = Point3D(to.x, from.y, to.z)

            for(key in facesJson.keySet())
                faces[key] = TextureCubeFace(key, this, facesJson.getJSONObject(key))

            if(json.has("shade"))
                shade = json.getBoolean("shade")
        }
    }

    private class TextureCubeFace(face: String, cube: TextureCube, json: JSONObject){
        var uv: ModelTexture.SubTexture
        var texture: String
        var cullFace = CullFace.FRONT
        var rotation = 0.0

        init{
            uv = if(json.has("uv")) {
                val uvJson = json.getJSONArray("uv")
                ModelTexture.SubTexture(uvJson.getDouble(0), uvJson.getDouble(1), uvJson.getDouble(2), uvJson.getDouble(3))
            }else {
                when(face){
                    // todo: Coordinates might be wrong
                    "up" -> ModelTexture.SubTexture(cube.backTopLeft.x * 16, cube.backTopLeft.z * 16, cube.frontTopRight.x * 16, cube.frontTopRight.z * 16)
                    "down" -> ModelTexture.SubTexture(cube.backBottomLeft.x * 16, cube.backBottomLeft.z * 16, cube.frontBottomRight.x * 16, cube.frontBottomRight.z * 16)
                    "north" -> ModelTexture.SubTexture(16 - cube.backTopRight.x * 16, 16 - cube.backTopRight.y * 16, 16 - cube.backBottomLeft.x * 16, 16 - cube.backBottomLeft.y * 16)
                    "south" -> ModelTexture.SubTexture(cube.frontTopLeft.x * 16, 16 - cube.frontTopLeft.y * 16, cube.frontBottomRight.x * 16, 16 - cube.frontBottomRight.y * 16)
                    "west" -> ModelTexture.SubTexture(cube.backTopLeft.z * 16, 16 - cube.backTopLeft.y * 16, cube.frontBottomLeft.z * 16, 16 - cube.frontBottomLeft.y * 16)
                    "east" -> ModelTexture.SubTexture(16 - cube.frontTopRight.z * 16, 16 - cube.frontTopRight.y * 16, 16 - cube.backBottomRight.z * 16, 16 - cube.backBottomRight.y * 16)
                    else -> ModelTexture.SubTexture(0.0, 0.0, 16.0, 16.0)
                }
            }

            texture = json.getString("texture")
            if(json.has("rotation"))
                rotation = json.getDouble("rotation")
        }
    }

    private class TextureVariable(var model: Model, var name: String, var value: String){

        fun get(): String{
            var actualValue = value
            if(value.startsWith("#"))
                actualValue = model.variables[value.substring(1)]!!.value
            return actualValue
        }

        fun claim(model: Model): TextureVariable = TextureVariable(model, name, value)

    }

    private class TexturePoint(x: Double, y: Double, z: Double): Point3D(x / 16, y / 16, z / 16)


}