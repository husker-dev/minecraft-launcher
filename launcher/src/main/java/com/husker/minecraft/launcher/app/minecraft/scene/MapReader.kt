package com.husker.minecraft.launcher.app.minecraft.scene

import com.husker.minecraft.launcher.app.minecraft.Block
import javafx.geometry.Point3D
import java.io.InputStream
import java.nio.charset.StandardCharsets

class MapReader(
    stream: InputStream,
    applier: (String, Point3D, HashMap<Block.Side, Int>, Map<String, String>) -> Unit) {

    private val partSeparator = 0.toByte()
    private val separator = 1.toByte()

    private val contentBytes = stream.readBytes()

    init{
        val parts = contentBytes.split(partSeparator, maxParts = 5)

        val radius = parts[0].toInt()
        val names = parts[1].split(separator).map { it.toText() }
        val metaKeys = parts[2].split(separator).map { it.toText() }
        val metaValues = parts[3].split(separator).map { it.toText() }

        parts[4].split(separator, minSize = 7).forEach {
            try {
                val name = names[it[0].toNum()]
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

                val data = if(it.size > 7) {
                     it.copyOfRange(7, it.size).toCollection(ArrayList())
                         .chunked(2)
                         .associate { value -> metaKeys[value[0].toNum()] to metaValues[value[1].toNum()] }
                }else hashMapOf()

                applier.invoke(name, position, light, data)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    private fun Byte.leftPart(): Int = this.toNum() and 240 shr 4   // mask 11110000, and move to right
    private fun Byte.rightPart(): Int = this.toNum() and 15         // mask 00001111
    private fun Byte.toNum(): Int = this.toInt() - 2                // -2 to avoid conflicts with separators

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