package com.husker.minecraft.launcher.plugin


import org.bukkit.ChatColor
import org.bukkit.block.Block
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import java.io.File
import java.lang.UnsupportedOperationException
import java.nio.charset.StandardCharsets

class AreaGenerator(private val sender: CommandSender, plugin: Plugin, x: Int, y: Int, z: Int, radius: Int, private val sky: Boolean) {

    init {
        val content = ByteWriter()
        val blocks = ByteWriter()

        val blockNamesSet = CachedHashSet<String>()
        val blockDataKeySet = CachedHashSet<String>()
        val blockDataValuesSet = CachedHashSet<String>()

        for(lx in x-radius..x+radius){
            for(ly in y-radius..y+radius){
                for(lz in z-radius..z+radius){
                    val block = getBlock(lx, ly, lz)
                    val name = block.toString().split("data=Block{")[1].split(":")[1].split("}")[0]

                    if(name == "air" || name == "void_air")
                        continue

                    val info = ByteWriter()

                    // Name
                    info.addInt(blockNamesSet.addAndGetIndex(name))

                    // Coordinates
                    info.addInt(lx - (x-radius))    // X
                    info.addInt(ly - (y-radius))    // Y
                    info.addInt(lz - (z-radius))    // Z

                    // Light
                    info.addInt(lightPairAsInt(getBlock(lx, ly, lz - 1), getBlock(lx, ly, lz + 1)))    // Front, Back
                    info.addInt(lightPairAsInt(getBlock(lx + 1, ly, lz), getBlock(lx - 1, ly, lz)))    // Right, Left
                    info.addInt(lightPairAsInt(getBlock(lx, ly + 1, lz), getBlock(lx, ly - 1, lz)))    // Top, Bottom

                    // Meta
                    if("[" in block.blockData.toString()) {
                        for (dataPair in block.blockData.toString().split("[")[1].split("]")[0].split(",")) {
                            val data = dataPair.split("=")

                            info.addInt(blockDataKeySet.addAndGetIndex(data[0]))
                            info.addInt(blockDataValuesSet.addAndGetIndex(data[1]))
                        }
                    }

                    blocks.addBytes(info.toByteList())
                    if(lx != x+radius || ly != y+radius || lz!= z+radius)
                        blocks.separate()
                }
            }
        }

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

        val areas = File(plugin.server.worldContainer, "areas")
        areas.mkdirs()
        File(areas, "area_${x}_${y}_${z}.map").writeBytes(content.toByteArray())

        sender.sendMessage("${ChatColor.GREEN}Completed!")
    }

    private fun getBlock(x: Int, y: Int, z: Int): Block = (sender as Entity).world.getBlockAt(x, y, z)

    private fun getLight(block: Block): Int{
        return if(sky)
            block.lightLevel.toInt()
        else
            block.lightFromBlocks.toInt()
    }

    private fun lightPairAsInt(block1: Block, block2: Block): Int = (getLight(block1) shl 4) or getLight(block2)

    private class CachedHashSet<T>: LinkedHashSet<T>(){

        fun addAndGetIndex(value: T): Int{
            add(value)
            return indexOf(value)
        }
    }

    private class ByteWriter{

        private val partSeparator = 0.toByte()
        private val separator = 1.toByte()
        private var bytes = arrayListOf<Byte>()

        fun addInt(num: Int): ByteWriter {
            bytes.add((num + 2).toByte())
            return this
        }

        fun addString(str: String): ByteWriter{
            bytes.addAll(str.toByteArray(StandardCharsets.UTF_8).toTypedArray())
            return this
        }

        fun addBytes(bytes: List<Byte>): ByteWriter{
            this.bytes.addAll(bytes)
            return this
        }

        fun separate(): ByteWriter {
            bytes.add(separator)
            return this
        }

        fun newPart(): ByteWriter {
            bytes.add(partSeparator)
            return this
        }

        fun toByteList(): List<Byte> = bytes

        fun toByteArray(): ByteArray = bytes.toByteArray()
    }
}