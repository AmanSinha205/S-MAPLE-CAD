package org.smaple.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * S-MAPLE Color Palette
 * Defined in S-MAPLE UI/UX guidelines v1.0, Section 3.1.
 *
 * Light theme only in this phase.
 */
val Pine = Color(0xFF14503F)            // Primary buttons, token (ready, done), headings accents, active nav
val PineDeep = Color(0xFF0E3A2D)        // Pressed state, text on Mist chips
val Sage = Color(0xFFF2F6F1)            // Screen background (Ground)
val White = Color(0xFFFFFFFF)           // Surface: Cards, bottom bar, input fields
val Mist = Color(0xFFDCE9DE)            // Quiet fill: Chips, icon buttons, quiet highlights
val Mist2 = Color(0xFFC7DCCB)           // Selected / inactive progress, nav pill, token rings
val Line = Color(0xFFD3E0D5)            // 1 dp card and divider lines
val MapleAmber = Color(0xFFF2A33A)      // Attention: Something is working or waiting, token working, badges
val AmberTint = Color(0xFFFBE3BC)       // Attention tint: "Waiting" chips
val MapleRed = Color(0xFFB3321F)        // Problem: Problems only (token, error icon)
val RedTint = Color(0xFFF6D9D3)         // Problem tint: Error banners
val Ink = Color(0xFF14231C)             // Primary text
val Moss = Color(0xFF4B5E55)            // Secondary text: Captions, helper text

/**
 * Extended color role container for S-MAPLE specific semantically-named tokens.
 */
@Immutable
data class MapleExtendedColors(
    val pine: Color = Pine,
    val pineDeep: Color = PineDeep,
    val sage: Color = Sage,
    val white: Color = White,
    val mist: Color = Mist,
    val mist2: Color = Mist2,
    val line: Color = Line,
    val mapleAmber: Color = MapleAmber,
    val amberTint: Color = AmberTint,
    val mapleRed: Color = MapleRed,
    val redTint: Color = RedTint,
    val ink: Color = Ink,
    val moss: Color = Moss
)
