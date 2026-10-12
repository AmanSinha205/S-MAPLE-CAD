package org.smaple.mcard.ui.home

import org.smaple.core.designsystem.component.MapleTokenState

/**
 * Immutable UI state for mCard Home Screen (Patient view).
 */
data class MCardHomeUiState(
    val tokenState: MapleTokenState = MapleTokenState.Ready,
    val headline: String = "Ready",
    val instruction: String = "Hold against doctor's phone"
)
