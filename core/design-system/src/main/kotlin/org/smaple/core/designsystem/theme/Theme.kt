package org.smaple.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LightColorScheme = lightColorScheme(
    primary = Pine,
    onPrimary = White,
    primaryContainer = Mist,
    onPrimaryContainer = PineDeep,
    secondary = Pine,
    onSecondary = White,
    secondaryContainer = Mist2,
    onSecondaryContainer = Ink,
    background = Sage,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    surfaceVariant = Sage,
    onSurfaceVariant = Moss,
    outline = Line,
    outlineVariant = Mist2,
    error = MapleRed,
    onError = White,
    errorContainer = RedTint,
    onErrorContainer = Ink
)

val LocalMapleExtendedColors = staticCompositionLocalOf { MapleExtendedColors() }

/**
 * S-MAPLE design system theme.
 * Enforces accessible colors, Atkinson Hyperlegible typography, and standardized touch dimensions.
 */
@Composable
fun MapleTheme(
    content: @Composable () -> Unit
) {
    val extendedColors = MapleExtendedColors()

    CompositionLocalProvider(
        LocalMapleExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = MapleTypography,
            shapes = MapleShapes,
            content = content
        )
    }
}

object MapleTheme {
    val extendedColors: MapleExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalMapleExtendedColors.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes

    val spacing: MapleSpacing
        get() = MapleSpacing

    val dimensions: MapleDimensions
        get() = MapleDimensions
}
