package com.husker.minecraft.launcher.plugin.writer

import org.bukkit.World
import org.bukkit.block.Block
import java.io.File

class AreaFileWriter(
        private val radius: Int,
        private val pivotX: Int,
        private val pivotY: Int,
        private val pivotZ: Int,
        private val world: World,
        private val useSkyLight: Boolean
    ) {

    enum class Side {
        Front,
        Left,
        Right,
        Top,
        Bottom
    }

    private val blocks = ByteWriter()

    private val blockNamesSet = CachedHashSet<String>()
    private val blockDataKeySet = CachedHashSet<String>()
    private val blockDataValuesSet = CachedHashSet<String>()

    fun addBlock(block: Block, sides: Array<Side>){
        val x = block.x
        val y = block.y
        val z = block.z
        val name = block.toString().split("data=Block{")[1].split(":")[1].split("}")[0]

        if(name == "air" || name == "void_air")
            return

        val info = ByteWriter()

        // Name
        info.addInt(blockNamesSet.addAndGetIndex(name))

        // Coordinates
        info.addInt(x - (pivotX - radius))    // X
        info.addInt(y - (pivotY - radius))    // Y
        info.addInt(z - (pivotZ - radius))    // Z

        // Light
        info.addInt(lightPairAsInt(getBlock(x, y, z - 1), getBlock(x, y, z + 1)))    // Front, Back
        info.addInt(lightPairAsInt(getBlock(x + 1, y, z), getBlock(x - 1, y, z)))    // Right, Left
        info.addInt(lightPairAsInt(getBlock(x, y + 1, z), getBlock(x, y - 1, z)))    // Top, Bottom

        // Visible sides
        info.addInt(sidesAsInt(sides))

        // Meta
        if("[" in block.blockData.toString()) {
            for (dataPair in block.blockData.toString().split("[")[1].split("]")[0].split(",")) {
                val data = dataPair.split("=")

                info.addInt(blockDataKeySet.addAndGetIndex(data[0]))
                info.addInt(blockDataValuesSet.addAndGetIndex(data[1]))
            }
        }

        if(blocks.toByteList().isNotEmpty())    // If not first, put separator
            blocks.separate()
        blocks.addBytes(info.toByteList())
    }

    fun save(directory: File){
        val content = ByteWriter()

        // Radius
        content.addInt(radius)

        // Names
        content.newPart()
        blockNamesSet.forEachIndexed{ index, value ->
            content.addString(value)
            if(index < blockNamesSet.size - 1)
                content.separate()
        }

        // Meta keys
        content.newPart()
        blockDataKeySet.forEachIndexed{ index, value ->
            content.addString(value)
            if(index < blockDataKeySet.size - 1)
                content.separate()
        }

        // Meta values
        content.newPart()
        blockDataValuesSet.forEachIndexed{ index, value ->
            content.addString(value)
            if(index < blockDataValuesSet.size - 1)
                content.separate()
        }

        // Area
        content.newPart()
        content.addBytes(blocks.toByteList())

        directory.mkdirs()
        File(directory, "area_${pivotX}_${pivotY}_${pivotZ}.map").writeBytes(content.toByteArray())
    }

    private fun getBlock(x: Int, y: Int, z: Int): Block = world.getBlockAt(x, y, z)

    private fun getLight(block: Block): Int{
        return if(useSkyLight)
            block.lightLevel.toInt()
        else
            block.lightFromBlocks.toInt()
    }

    private fun lightPairAsInt(block1: Block, block2: Block): Int = (getLight(block1) shl 4) or getLight(block2)

    private fun sidesAsInt(sides: Array<Side>): Int{
        var result = 64

        if(sides.contains(Side.Left))
            result = result or 1
        if(sides.contains(Side.Right))
            result = result or (1 shl 1)
        if(sides.contains(Side.Top))
            result = result or (1 shl 2)
        if(sides.contains(Side.Bottom))
            result = result or (1 shl 3)
        if(sides.contains(Side.Front))
            result = result or (1 shl 4)

        return result
    }
}