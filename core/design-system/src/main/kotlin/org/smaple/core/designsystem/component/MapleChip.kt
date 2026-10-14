package org.smaple.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.smaple.core.designsystem.theme.AmberTint
import org.smaple.core.designsystem.theme.Ink
import org.smaple.core.designsystem.theme.Line
import org.smaple.core.designsystem.theme.MapleDimensions
import org.smaple.core.designsystem.theme.MapleLabel
import org.smaple.core.designsystem.theme.MapleSpacing
import org.smaple.core.designsystem.theme.MapleTheme
import org.smaple.core.designsystem.theme.Mist
import org.smaple.core.designsystem.theme.PillShape
import org.smaple.core.designsystem.theme.Pine
import org.smaple.core.designsystem.theme.PineDeep
import org.smaple.core.designsystem.theme.White

/**
 * Choice chip (Section 5): 56 dp pill.
 * Selected: Pine fill, white text, check icon.
 * Unselected: white fill, 2 dp Line border, Ink text.
 */
@Composable
fun MapleChoiceChip(
    text: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val containerColor = if (selected) Pine else White
    val contentColor = if (selected) White else Ink
    val border = if (selected) null else BorderStroke(2.dp, Line)

    Surface(
        shape = PillShape,
        color = containerColor,
        contentColor = contentColor,
        border = border,
        modifier = modifier
            .height(MapleDimensions.choiceChipHeight)
            .clip(PillShape)
            .clickable(enabled = enabled, role = Role.Checkbox) {
                onSelectedChange(!selected)
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = MapleSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(MapleSpacing.small))
            }
            Text(
                text = text,
                style = MapleLabel,
                color = contentColor
            )
        }
    }
}

/**
 * Data chip (Section 5): 34 dp pill, Mist fill, Pine deep text, 18 sp bold.
 * Used for vitals on notes (e.g. "Blood pressure 120/80", "Pulse 72").
 */
@Composable
fun MapleDataChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(MapleDimensions.dataChipHeight)
            .clip(PillShape)
            .background(Mist)
            .padding(horizontal = MapleSpacing.medium),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MapleLabel,
            color = PineDeep
        )
    }
}

/**
 * Status chip (Section 5): 34 dp pill, Amber tint fill, Ink text, 18 sp bold.
 * Used for waiting / in-progress states (e.g. "Waiting", "Uploading").
 */
@Composable
fun MapleStatusChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(MapleDimensions.statusChipHeight)
            .clip(PillShape)
            .background(AmberTint)
            .padding(horizontal = MapleSpacing.medium),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MapleLabel,
            color = Ink
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F1)
@Composable
private fun MapleChipsPreview() {
    MapleTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MapleChoiceChip(text = "5 Notes", selected = true, onSelectedChange = {})
                MapleChoiceChip(text = "10 Notes", selected = false, onSelectedChange = {})
                MapleChoiceChip(text = "20 Notes", selected = false, onSelectedChange = {})
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MapleDataChip(text = "Blood pressure 120/80")
                MapleDataChip(text = "Pulse 72")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MapleStatusChip(text = "Waiting")
                MapleStatusChip(text = "Uploading")
            }
        }
    }
}
