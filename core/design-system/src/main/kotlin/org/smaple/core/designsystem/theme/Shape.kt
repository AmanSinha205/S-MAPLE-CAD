package org.smaple.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape scale defined in S-MAPLE UI/UX guidelines v1.0, Section 3.3.
 */
val PillShape = CircleShape
val CardShape = RoundedCornerShape(24.dp)
val ListCardShape = RoundedCornerShape(20.dp)
val InputFieldShape = RoundedCornerShape(16.dp)
val BannerShape = RoundedCornerShape(18.dp)

val MapleShapes = Shapes(
    small = InputFieldShape,
    medium = ListCardShape,
    large = CardShape,
    extraLarge = PillShape
)
