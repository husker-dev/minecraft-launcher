package com.husker.minecraft.launcher.app.minecraft.scene

import com.husker.minecraft.launcher.app.minecraft.blocks.Block
import javafx.geometry.Point3D
import java.io.InputStream
import java.nio.charset.StandardCharsets

class MapReader(
    stream: InputStream,
    applier: (String, Point3D, HashMap<Block.Side, Int>, List<Block.Side>, Map<String, String>) -> Unit) {

    private val partSeparator = 0.toByte()
    private val separator = 1.toByte()

    private val contentBytes = stream.readBytes()

    init{
        val parts = contentBytes.split(partSeparator, maxParts = 5)

        val radius = parts[0].toInt()
        val names = parts[1].split(separator).map { it.toText() }
        val metaKeys = parts[2].split(separator).map { it.toText() }
        val metaValues = parts[3].split(separator).map { it.toText() }

        parts[4].split(separator, minSize = 8).forEach {
            var name = "[unknown]"
            try {
                name = names[it[0].toNum()]
                val position = Point3D(
                    it[1].toNum().toDouble() - radius,
                    it[2].toNum().toDouble() - radius,
                    it[3].toNum().toDouble() - radius
                )

                val light = hashMapOf(
                    Block.Side.Face to it[4].leftPart(),
                    Block.Side.Back to it[4].rightPart(),
                    Block.Side.Left to it[5].leftPart(),    // Left and Right are reversed
                    Block.Side.Right to it[5].rightPart(),
                    Block.Side.Top to it[6].leftPart(),
                    Block.Side.Bottom to it[6].rightPart()
                )

                val sides = it[7].toSides()

                val data = try {
                    if (it.size > 8) {
                        it.copyOfRange(8, it.size).toCollection(ArrayList())
                            .chunked(2)
                            .associate { value -> metaKeys[value[0].toNum()] to metaValues[value[1].toNum()] }
                    } else
                        hashMapOf()
                }catch (e: Exception){
                    emptyMap()
                }

                applier.invoke(name, position, light, sides, data)
            }catch (e: Exception){
                println("Can't load block: $name")
                e.printStackTrace()
            }
        }
    }

    private fun Byte.leftPart(): Int = this.toNum() and 240 shr 4   // mask 11110000, and move to right
    private fun Byte.rightPart(): Int = this.toNum() and 15         // mask 00001111
    private fun Byte.toNum(): Int = this.toInt() - 2                // -2 to avoid conflicts with separators

    private fun Byte.toSides(): List<Block.Side>{
        val sides = arrayListOf<Block.Side>()
        // 1 = 00000001
        if(this.toNum() and 1 == 1) sides.add(Block.Side.Left)
        if(this.toNum() shr 1 and 1 == 1) sides.add(Block.Side.Right)
        if(this.toNum() shr 2 and 1 == 1) sides.add(Block.Side.Top)
        if(this.toNum() shr 3 and 1 == 1) sides.add(Block.Side.Bottom)
        if(this.toNum() shr 4 and 1 == 1) sides.add(Block.Side.Face)
        return sides
    }

    private fun ByteArray.split(delimiter: Byte, maxParts: Int = Int.MAX_VALUE, minSize: Int = 0): Array<ByteArray>{
        val parts = arrayListOf<ByteArray>()
        var buffer = arrayListOf<Byte>()

        for(byte in this){
            if(byte == delimiter && buffer.size >= minSize && parts.size != maxParts - 1){
                parts.add(buffer.toByteArray())
                buffer = arrayListOf()
            }else
                buffer.add(byte)
        }
        if(buffer.size > 0)
            parts.add(buffer.toByteArray())
        return parts.toTypedArray()
    }

    private fun ByteArray.toInt(): Int{
        return this[0].toNum()
    }

    private fun ByteArray.toText(): String{
        return String(this, StandardCharsets.UTF_8)
    }

}