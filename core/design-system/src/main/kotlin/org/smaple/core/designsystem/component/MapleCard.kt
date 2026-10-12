package org.smaple.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.smaple.core.designsystem.theme.CardShape
import org.smaple.core.designsystem.theme.Line
import org.smaple.core.designsystem.theme.ListCardShape
import org.smaple.core.designsystem.theme.MapleBody
import org.smaple.core.designsystem.theme.MapleDimensions
import org.smaple.core.designsystem.theme.MapleSpacing
import org.smaple.core.designsystem.theme.MapleTheme
import org.smaple.core.designsystem.theme.MapleTitle
import org.smaple.core.designsystem.theme.White

/**
 * Standard S-MAPLE card surface.
 *
 * Implements the flat, shadow-free card aesthetic with a 1 dp Line border.
 *
 * @param isListCard When true, uses 20 dp corner radius; when false, uses standard 24 dp radius.
 * @param innerPadding Internal padding for card contents, defaults to 20 dp.
 */
@Composable
fun MapleCard(
    modifier: Modifier = Modifier,
    isListCard: Boolean = false,
    innerPadding: Dp = MapleSpacing.group,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = if (isListCard) ListCardShape else CardShape

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = White,
        border = BorderStroke(width = MapleDimensions.borderWidth, color = Line),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(innerPadding),
            content = content
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F1)
@Composable
private fun MapleCardPreview() {
    MapleTheme {
        Column(
            modifier = Modifier.padding(MapleSpacing.screenPadding)
        ) {
            MapleCard {
                Text(text = "Patient summary", style = MapleTitle)
                Text(
                    text = "Asha Sharma, 42 years old. Blood group O+. 2 notes stored.",
                    style = MapleBody,
                    modifier = Modifier.padding(top = MapleSpacing.extraSmall)
                )
            }
        }
    }
}
