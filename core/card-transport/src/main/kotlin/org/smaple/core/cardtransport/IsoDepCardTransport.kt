package org.smaple.core.cardtransport

import android.nfc.TagLostException
import android.nfc.tech.IsoDep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Real Android implementation of [CardTransport] using [IsoDep] technology (ISO 14443-4).
 * Used for communicating with physical smartcards and Android Host Card Emulation (HCE) targets.
 */
class IsoDepCardTransport(
    private val isoDep: IsoDep,
    private val timeoutMs: Int = DEFAULT_TIMEOUT_MS
) : CardTransport {

    override val isConnected: Boolean
        get() = try {
            isoDep.isConnected
        } catch (_: Exception) {
            false
        }

    /**
     * Establishes connection to the IsoDep card target and sets communication timeout.
     *
     * @throws CardTransportException if connection fails, times out, or card is lost.
     */
    override suspend fun connect(): Unit = withContext(Dispatchers.IO) {
        try {
            if (!isoDep.isConnected) {
                isoDep.connect()
            }
            isoDep.timeout = timeoutMs
        } catch (e: TagLostException) {
            throw CardTransportException("Contactless card was lost or disconnected during connection", e)
        } catch (e: IOException) {
            throw CardTransportException("Failed to establish connection to IsoDep card: ${e.message}", e)
        } catch (e: Exception) {
            throw CardTransportException("Unexpected error connecting to IsoDep card: ${e.message}", e)
        }
    }

    /**
     * Closes the active IsoDep connection session.
     */
    override suspend fun close(): Unit = withContext(Dispatchers.IO) {
        try {
            if (isoDep.isConnected) {
                isoDep.close()
            }
        } catch (_: Exception) {
            // Safe cleanup on disconnection
        }
    }

    /**
     * Transceives a raw command APDU byte array and awaits the card response APDU.
     *
     * @param commandApdu The raw command APDU bytes to transmit.
     * @return Raw response APDU bytes including status word (SW1-SW2).
     * @throws CardTransportException on communication failure, timeout, or card disconnection.
     */
    override suspend fun transceive(commandApdu: ByteArray): ByteArray = withContext(Dispatchers.IO) {
        if (!isoDep.isConnected) {
            throw CardTransportException("Cannot transceive: IsoDep connection is closed or card was removed")
        }
        try {
            isoDep.transceive(commandApdu)
        } catch (e: TagLostException) {
            throw CardTransportException("Card disconnected or moved out of NFC field during transceive", e)
        } catch (e: IOException) {
            throw CardTransportException("I/O error during transceive (possible timeout or card reset): ${e.message}", e)
        } catch (e: Exception) {
            throw CardTransportException("Unexpected error during transceive: ${e.message}", e)
        }
    }

    /**
     * Helper that transceives a [CommandApdu] and returns the parsed [ResponseApdu].
     *
     * @throws CardTransportException if transmission fails or response is malformed.
     */
    suspend fun transceiveApdu(command: CommandApdu): ResponseApdu {
        val rawResponse = transceive(command.toByteArray())
        if (rawResponse.size < 2) {
            throw CardTransportException("Response APDU too short: expected at least 2 bytes, got ${rawResponse.size}")
        }
        return ResponseApdu.fromBytes(rawResponse)
    }

    companion object {
        const val DEFAULT_TIMEOUT_MS: Int = 3000
    }
}
