package org.smaple.core.cardtransport

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Test

class CardTransportTest {

    private val fakeTransport = InMemoryFakeCardTransport()

    @Test
    fun testInitialConnectionStateIsFalse() {
        assertFalse(fakeTransport.isConnected)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testConnectThrowsUnsupportedOperationInMilestone0() = runTest {
        fakeTransport.connect()
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testTransceiveThrowsUnsupportedOperationInMilestone0() = runTest {
        fakeTransport.transceive(byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00))
    }

    @Test
    fun testCloseSucceedsCleanly() = runTest {
        fakeTransport.close()
        assertFalse(fakeTransport.isConnected)
    }
}
