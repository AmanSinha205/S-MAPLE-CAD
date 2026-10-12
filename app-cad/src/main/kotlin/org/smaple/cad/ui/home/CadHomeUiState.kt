package org.smaple.cad.ui.home

import org.smaple.core.designsystem.component.MapleTokenState

/**
 * Immutable UI state for CAD Home Screen.
 */
data class CadHomeUiState(
    val tokenState: MapleTokenState = MapleTokenState.Ready,
    val headline: String = "Tap patient's mCard",
    val helperText: String = "Hold phone flat against card to view notes and record vitals",
    val isScanning: Boolean = false
)
