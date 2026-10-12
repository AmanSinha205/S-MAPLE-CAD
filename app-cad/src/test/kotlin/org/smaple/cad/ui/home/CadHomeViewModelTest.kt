package org.smaple.cad.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test
import org.smaple.core.designsystem.component.MapleTokenState

class CadHomeViewModelTest {

    @Test
    fun testInitialUiStateIsReady() {
        val viewModel = CadHomeViewModel()
        val state = viewModel.uiState.value

        assertEquals(MapleTokenState.Ready, state.tokenState)
        assertEquals("Tap patient's mCard", state.headline)
    }

    @Test
    fun testOnScanRequestedUpdatesUiState() {
        val viewModel = CadHomeViewModel()
        viewModel.onScanRequested()
        val state = viewModel.uiState.value

        assertEquals(MapleTokenState.Ready, state.tokenState)
        assertEquals("Ready to scan", state.headline)
    }
}
