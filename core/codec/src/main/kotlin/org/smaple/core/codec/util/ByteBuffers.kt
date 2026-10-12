package org.smaple.core.codec.util

import java.nio.charset.StandardCharsets

/**
 * Utility functions for big-endian binary operations and ASCII encoding/decoding.
 * Pure Kotlin JVM with zero Android dependencies.
 */
object ByteBuffers {

    private val ASCII = StandardCharsets.US_ASCII

    fun encodeAscii(text: String): ByteArray = text.toByteArray(ASCII)

    fun decodeAscii(bytes: ByteArray, offset: Int = 0, length: Int = bytes.size): String =
        String(bytes, offset, length, ASCII)

    fun putUint16BigEndian(buffer: ByteArray, offset: Int, value: Int) {
        buffer[offset] = ((value ushr 8) and 0xFF).toByte()
        buffer[offset + 1] = (value and 0xFF).toByte()
    }

    fun getUint16BigEndian(buffer: ByteArray, offset: Int): Int {
        val b0 = buffer[offset].toInt() and 0xFF
        val b1 = buffer[offset + 1].toInt() and 0xFF
        return (b0 shl 8) or b1
    }

    fun putUint24BigEndian(buffer: ByteArray, offset: Int, value: Long) {
        buffer[offset] = ((value ushr 16) and 0xFF).toByte()
        buffer[offset + 1] = ((value ushr 8) and 0xFF).toByte()
        buffer[offset + 2] = (value and 0xFF).toByte()
    }

    fun getUint24BigEndian(buffer: ByteArray, offset: Int): Long {
        val b0 = (buffer[offset].toLong() and 0xFF) shl 16
        val b1 = (buffer[offset + 1].toLong() and 0xFF) shl 8
        val b2 = buffer[offset + 2].toLong() and 0xFF
        return b0 or b1 or b2
    }
}
