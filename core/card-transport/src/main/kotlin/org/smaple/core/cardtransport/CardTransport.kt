package org.smaple.core.cardtransport

/**
 * Low-level transport interface for contactless smartcard and HCE communication.
 * All operations are suspending and non-blocking.
 */
interface CardTransport {

    /**
     * Whether the transport currently has an active connection session.
     */
    val isConnected: Boolean

    /**
     * Establishes a connection with the contactless card or emulated target.
     *
     * @throws CardTransportException if connection cannot be established or times out.
     */
    suspend fun connect()

    /**
     * Closes the active connection and releases underlying hardware or virtual resources.
     */
    suspend fun close()

    /**
     * Transceives a command APDU to the card target and awaits the card response APDU.
     *
     * @param commandApdu The raw command APDU bytes to transmit.
     * @return The raw response APDU bytes including status word (SW1-SW2).
     * @throws CardTransportException if communication fails or times out.
     */
    suspend fun transceive(commandApdu: ByteArray): ByteArray
}

/**
 * Base exception for card transport layer communication errors.
 */
open class CardTransportException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
