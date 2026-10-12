package org.smaple.core.codec

/**
 * Result wrapper for codec encode and decode operations.
 */
sealed interface CodecResult<out T> {
    data class Success<out T>(val value: T) : CodecResult<T>
    data class Error(val message: String, val cause: Throwable? = null) : CodecResult<Nothing>
}
