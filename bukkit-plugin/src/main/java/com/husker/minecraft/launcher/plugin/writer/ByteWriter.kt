package com.husker.minecraft.launcher.plugin.writer

import java.nio.charset.StandardCharsets
import java.util.*

class ByteWriter{
    companion object{
        private const val partSeparator = 0.toByte()
        private const val separator = 1.toByte()
    }

    private var bytes = Collections.synchronizedList(arrayListOf<Byte>())

    fun addInt(num: Int): ByteWriter {
        bytes.add((num + 2).toByte())
        return this
    }

    fun addString(str: String): ByteWriter {
        bytes.addAll(str.toByteArray(StandardCharsets.UTF_8).toTypedArray())
        return this
    }

    fun addBytes(bytes: List<Byte>): ByteWriter {
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