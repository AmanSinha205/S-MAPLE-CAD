package org.smaple.core.cardtransport

/**
 * In-memory test double / placeholder transport for Milestone 0.
 *
 * Real NFC IsoDep transport will be implemented in subsequent milestones per project plan.
 */
class InMemoryFakeCardTransport : CardTransport {

    private var connected: Boolean = false

    override val isConnected: Boolean
        get() = connected

    override suspend fun connect() {
        throw UnsupportedOperationException(
            "InMemoryFakeCardTransport is a placeholder for Milestone 0. NFC IsoDep transport will be implemented in subsequent milestones."
        )
    }

    override suspend fun close() {
        connected = false
    }

    override suspend fun transceive(commandApdu: ByteArray): ByteArray {
        throw UnsupportedOperationException(
            "InMemoryFakeCardTransport is a placeholder for Milestone 0. NFC IsoDep transport will be implemented in subsequent milestones."
        )
    }
}
