package org.smaple.core.codec

/**
 * Generic contract for encoding domain models into protocol binary representations
 * and decoding byte sequences back into domain models.
 */
interface ProtocolCodec<T> {
    /**
     * Serializes domain model [value] to its byte array protocol representation.
     */
    fun encode(value: T): ByteArray

    /**
     * Deserializes raw byte array [bytes] into a domain model wrapped in [CodecResult].
     */
    fun decode(bytes: ByteArray): CodecResult<T>
}
