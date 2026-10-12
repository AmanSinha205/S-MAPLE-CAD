package org.smaple.core.designsystem

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.smaple.core.designsystem.component.MapleTokenState
import org.smaple.core.designsystem.theme.MapleDimensions
import org.smaple.core.designsystem.theme.MapleSpacing
import org.smaple.core.designsystem.theme.Pine

class ThemeTest {

    @Test
    fun testSpacingConstants() {
        assertNotNull(MapleSpacing.screenPadding)
        assertNotNull(MapleSpacing.group)
        assertNotNull(MapleDimensions.minTouchTarget)
    }

    @Test
    fun testTokenStates() {
        assertEquals("Ready", MapleTokenState.Ready.stateWord)
        assertEquals("Working", MapleTokenState.Working.stateWord)
        assertEquals("Done", MapleTokenState.Done.stateWord)
        assertEquals("Problem", MapleTokenState.Problem.stateWord)
    }

    @Test
    fun testPineColorValue() {
        assertEquals(Color(0xFF14503F), Pine)
    }
}
