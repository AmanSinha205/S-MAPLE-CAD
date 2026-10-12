package org.smaple.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard dimensions defined in S-MAPLE UI/UX guidelines v1.0, Section 3.3 and 5.
 */
@Immutable
object MapleDimensions {
    // Touch Targets
    val minTouchTarget: Dp = 48.dp

    // Button Heights
    val primaryButtonHeight: Dp = 64.dp
    val primaryButtonFlowHeight: Dp = 72.dp
    val secondaryButtonHeight: Dp = 56.dp
    val amberButtonHeight: Dp = 54.dp
    val textButtonHeight: Dp = 52.dp
    val roundIconButtonSize: Dp = 56.dp

    // Field Heights
    val inputFieldHeight: Dp = 56.dp
    val notesBoxHeight: Dp = 84.dp

    // Navigation & Container Heights
    val bottomBarHeight: Dp = 96.dp
    val maxContentWidth: Dp = 600.dp

    // Borders & Dividers
    val borderWidth: Dp = 1.dp
    val inputBorderWidth: Dp = 2.dp
    val tokenRingStroke: Dp = 3.dp

    // Token Sizes (Section 4.2)
    val tokenSizeHero: Dp = 236.dp
    val tokenSizeHome: Dp = 188.dp
    val tokenSizeSave: Dp = 176.dp
    val tokenSizeWelcome: Dp = 148.dp
    val tokenSizeInline: Dp = 92.dp
}
