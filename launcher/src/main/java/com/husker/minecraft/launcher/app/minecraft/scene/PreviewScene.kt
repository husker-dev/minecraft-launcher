package com.husker.minecraft.launcher.app.minecraft.scene

import com.husker.minecraft.launcher.app.minecraft.Block
import com.husker.minecraft.launcher.app.minecraft.models.ModelTexture
import com.husker.minecraft.launcher.app.minecraft.blocks.impl.AirBlock
import com.husker.minecraft.launcher.app.minecraft.MineVersion
import com.husker.minecraft.launcher.tools.fx.LauncherTimer
import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.geometry.Point3D
import javafx.scene.*
import javafx.scene.paint.Color
import javafx.scene.transform.Rotate
import javafx.scene.transform.Scale
import javafx.scene.transform.Translate
import org.json.JSONObject
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


class PreviewScene : SubScene(Group(), 0.0, 0.0, true, SceneAntialiasing.BALANCED) {

    private val blockSize = 1.0

    private val content = root as Group
    private val camera = PerspectiveCamera(true)
    private val blocks = arrayListOf<SceneBlock>()
    private val blocksCoordinates = HashMap<Int, HashMap<Int, HashMap<Int, SceneBlock>>>()

    private val rotationX = Rotate(0.0, Rotate.Y_AXIS)
    private val rotationY = Rotate(0.0, Rotate.X_AXIS)

    init{
        camera.translateZ = -17.0
        camera.nearClip = 0.1
        camera.fieldOfView = 50.0
        camera.farClip = 100.0
        camera.rotationAxis = Point3D.ZERO
        camera.rotate = 45.0
        setCamera(camera)

        rotationX.pivotY = 4 * blockSize
        rotationY.pivotY = 4 * blockSize
        content.transforms.add(rotationX)
        content.transforms.add(rotationY)

        // First texture ALWAYS has enabled linear antialiasing, so create the empty one
        // Also first added texture creates a little freeze, so pre-creation is the good solution
        content.children.add(ModelTexture.empty)

        var camAnimationFrame = 0.0.toBigDecimal()
        LauncherTimer.create(10){
            val valDist = 300
            val posMultX = 6
            val posMultY = 6

            val curX = sin(camAnimationFrame.toDouble() / valDist) * posMultX
            val curY = cos(camAnimationFrame.toDouble() / valDist) * posMultY

            Platform.runLater { rotate(curX, curY) }
            camAnimationFrame++
        }
    }

    fun rotate(x : Double, y : Double){
        rotationX.angle = x
        rotationY.angle = y
    }

    fun clear(){
        blocks.clear()
    }

    fun applyJSON(version: MineVersion, content: JSONObject){
        clear()
        val area = content.getJSONArray("area")
        val blockNamesMap = content.getJSONObject("names_map")
        val blockDataKeyMap = content.getJSONObject("data_keys_map")
        val blockDataValuesMap = content.getJSONObject("data_values_map")

        for(i in 0 until area.length()){
            try {
                val blockInfo = area.getJSONObject(i)
                val name = blockNamesMap.getString(blockInfo.getInt("n").toString())

                val positionInfo = blockInfo.getJSONArray("p")
                val lightInfo = blockInfo.getJSONArray("l")
                var blockData = mapOf<String, String>()
                if(blockInfo.has("d")) {
                    blockData = blockInfo.getJSONObject("d").toMap().map {
                        blockDataKeyMap.getString(it.key) to blockDataValuesMap.getString(it.value.toString())
                    }.toMap()
                }

                val lights = hashMapOf(
                    Block.Side.Face to lightInfo.getInt(0),
                    Block.Side.Left to lightInfo.getInt(2), // Left and Right are reversed
                    Block.Side.Right to lightInfo.getInt(1),
                    Block.Side.Back to lightInfo.getInt(3),
                    Block.Side.Top to lightInfo.getInt(4),
                    Block.Side.Bottom to lightInfo.getInt(5)
                )

                addBlock(
                    version.getBlockInstance(name, lights, blockData),
                    positionInfo.getInt(0),
                    positionInfo.getInt(1),
                    positionInfo.getInt(2)
                )
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        recalculateBlocks()
    }

    fun addBlock(block: Block, x: Int, y: Int, z: Int){
        val sceneBlock = SceneBlock(block, -x, y, z)

        if(!blocksCoordinates.containsKey(x))
            blocksCoordinates[x] = HashMap()
        if(!blocksCoordinates[x]!!.containsKey(y))
            blocksCoordinates[x]!![y] = HashMap()

        blocksCoordinates[x]!![y]!![z] = sceneBlock

        blocks.add(sceneBlock)
    }

    fun recalculateBlocks(){
        Platform.runLater {
            content.children.removeIf { content.children.indexOf(it) > 0 }
            content.children.add(AmbientLight(Color(1.0, 1.0, 1.0, 1.0)))
        }

        val defaultBlockTransforms = arrayListOf(
            Scale(blockSize, blockSize, blockSize),
            Translate(-blockSize/2, -blockSize/2, -blockSize/2)
        )

        val allBlocks = arrayListOf<Node>()
        blocks.sortedWith{ a, b ->
            val queue = a.originalBlock.renderQueuePos.compareTo(b.originalBlock.renderQueuePos)
            return@sortedWith if(queue != 0)
                queue
            else {
                a.distance.compareTo(b.distance)
            }
        }.forEach {
            try {
                val nearSides = arrayListOf<Block.Side>()

                val frontBlock = getBlockAt(it.x, it.y, it.z - 1)
                val backBlock = getBlockAt(it.x, it.y, it.z + 1)
                val leftBlock = getBlockAt(it.x - 1, it.y, it.z)
                val rightBlock = getBlockAt(it.x + 1, it.y, it.z)
                val topBlock = getBlockAt(it.x, it.y + 1, it.z)
                val bottomBlock = getBlockAt(it.x, it.y - 1, it.z)

                if (!frontBlock.getFilledSides().contains(Block.Side.Back))
                    nearSides.add(Block.Side.Face)
                if (!backBlock.getFilledSides().contains(Block.Side.Face))
                    nearSides.add(Block.Side.Back)
                if (!leftBlock.getFilledSides().contains(Block.Side.Right))
                    nearSides.add(Block.Side.Left)
                if (!rightBlock.getFilledSides().contains(Block.Side.Left))
                    nearSides.add(Block.Side.Right)
                if (!topBlock.getFilledSides().contains(Block.Side.Bottom))
                    nearSides.add(Block.Side.Top)
                if (!bottomBlock.getFilledSides().contains(Block.Side.Top))
                    nearSides.add(Block.Side.Bottom)

                if (nearSides.isEmpty())
                    return@forEach

                val blockModel = it.originalBlock.getNode(
                    nearSides,
                    Point3D(it.x.toDouble(), it.y.toDouble(), it.z.toDouble())
                )
                blockModel.transforms.addAll(defaultBlockTransforms)
                blockModel.transforms.add(
                    Translate(
                        it.x.toDouble() * blockSize,
                        -(it.y.toDouble() - 4) * blockSize,
                        it.z.toDouble() * blockSize
                    )
                )

                allBlocks.add(blockModel)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        pushToScene(allBlocks)
    }

    // TODO: Pushing to scene is a bit laggy
    private fun pushToScene(nodes: ArrayList<Node>){
        object : AnimationTimer() {
            override fun handle(now: Long) {
                synchronized(nodes) {
                    val part = 40
                    val subArray = ArrayList(nodes.subList(0, min(nodes.size, part)))
                    nodes.removeAll(subArray)

                    subArray.forEach { group ->
                        (group as Group).children.forEach {
                            (it as ModelTexture).startAnimation()
                        }
                    }
                    content.children.addAll(subArray)

                    if(nodes.size == 0)
                        stop()
                }
            }
        }.start()
    }

    private fun getBlockAt(x: Int, y: Int, z: Int): Block {
        return try {
            blocksCoordinates[-x]!![y]!![z]!!.originalBlock
        }catch (e: Exception){
            AirBlock()
        }
    }


    data class SceneBlock(val originalBlock: Block, val x: Int, val y: Int, val z: Int){

        companion object{
            val startPoint = Point3D(0.0, 3.0, -3.0)
        }

        val distance: Double by lazy {
            startPoint.distance(Point3D(x.toDouble(), y.toDouble(), z.toDouble()))
        }

    }
}