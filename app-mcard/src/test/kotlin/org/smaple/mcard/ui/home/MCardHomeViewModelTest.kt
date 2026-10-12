package org.smaple.mcard.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test
import org.smaple.core.designsystem.component.MapleTokenState

class MCardHomeViewModelTest {

    @Test
    fun testInitialUiStateIsReady() {
        val viewModel = MCardHomeViewModel()
        val state = viewModel.uiState.value

        assertEquals(MapleTokenState.Ready, state.tokenState)
        assertEquals("Ready", state.headline)
        assertEquals("Hold against doctor's phone", state.instruction)
    }
}
