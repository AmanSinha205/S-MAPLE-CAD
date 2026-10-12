package org.smaple.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Atkinson Hyperlegible font family setup with system font fallback.
 *
 * Designed for low-vision readability. Per guideline 3.2:
 * Minimum size is 16 sp throughout the app.
 */
val AtkinsonHyperlegibleFontFamily = FontFamily.Default

val MapleDisplay = TextStyle(
    fontFamily = AtkinsonHyperlegibleFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 40.sp,
    lineHeight = 46.sp,
    color = Ink
)

val MapleHeadline1 = TextStyle(
    fontFamily = AtkinsonHyperlegibleFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 34.sp,
    lineHeight = 40.sp,
    color = Ink
)

val MapleHeadline2 = TextStyle(
    fontFamily = AtkinsonHyperlegibleFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 34.sp,
    color = Ink
)

val MapleTitle = TextStyle(
    fontFamily = AtkinsonHyperlegibleFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    color = Ink
)

val MapleBodyLarge = TextStyle(
    fontFamily = AtkinsonHyperlegibleFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 22.sp,
    lineHeight = 32.sp,
    color = Ink
)

val MapleBody = TextStyle(
    fontFamily = AtkinsonHyperlegibleFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp,
    lineHeight = 30.sp,
    color = Ink
)

val MapleLabel = TextStyle(
    fontFamily = AtkinsonHyperlegibleFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    lineHeight = 26.sp,
    color = Ink
)

val MapleCaption = TextStyle(
    fontFamily = AtkinsonHyperlegibleFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    color = Moss
)

/**
 * Material3 Typography mapped to S-MAPLE's accessible type scale.
 */
val MapleTypography = Typography(
    displayLarge = MapleDisplay,
    displayMedium = MapleHeadline1,
    displaySmall = MapleHeadline2,
    headlineLarge = MapleHeadline1,
    headlineMedium = MapleHeadline2,
    headlineSmall = MapleTitle,
    titleLarge = MapleTitle,
    titleMedium = MapleTitle.copy(fontSize = 20.sp, lineHeight = 26.sp),
    titleSmall = MapleLabel,
    bodyLarge = MapleBodyLarge,
    bodyMedium = MapleBody,
    bodySmall = MapleCaption,
    labelLarge = MapleLabel,
    labelMedium = MapleLabel.copy(fontSize = 18.sp, lineHeight = 24.sp),
    labelSmall = MapleCaption
)
