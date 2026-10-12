package org.smaple.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing constants based on S-MAPLE 4 dp grid (Section 3.3).
 */
@Immutable
object MapleSpacing {
    val none: Dp = 0.dp
    val base: Dp = 4.dp
    val extraSmall: Dp = 8.dp
    val small: Dp = 12.dp
    val medium: Dp = 16.dp
    val group: Dp = 20.dp
    val screenPadding: Dp = 24.dp
    val extraLarge: Dp = 32.dp
    val doubleScreen: Dp = 48.dp
}
