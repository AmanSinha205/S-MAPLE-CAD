package org.smaple.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.smaple.core.designsystem.theme.Line
import org.smaple.core.designsystem.theme.MapleBody
import org.smaple.core.designsystem.theme.MapleDimensions
import org.smaple.core.designsystem.theme.MapleSpacing
import org.smaple.core.designsystem.theme.MapleTheme
import org.smaple.core.designsystem.theme.MapleTitle
import org.smaple.core.designsystem.theme.Moss
import org.smaple.core.designsystem.theme.White

/**
 * Standard list row (Section 5).
 * - Minimum 64 dp tall.
 * - Bold title (21 sp), Moss subtitle (18 sp).
 * - Trailing element (chips or metadata).
 * - 1 dp Line divider.
 */
@Composable
fun MapleListRow(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val rowModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Column(
        modifier = rowModifier
            .fillMaxWidth()
            .background(White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MapleDimensions.listRowMinHeight)
                .padding(
                    horizontal = MapleSpacing.screenPadding,
                    vertical = MapleSpacing.medium
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MapleTitle
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MapleBody,
                        color = Moss
                    )
                }
            }

            if (trailingContent != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = MapleSpacing.medium)
                ) {
                    trailingContent()
                }
            }
        }

        Divider(
            color = Line,
            thickness = MapleDimensions.borderWidth
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MapleListRowPreview() {
    MapleTheme {
        Column {
            MapleListRow(
                title = "12 Oct 2026, 10:20",
                subtitle = "Dr. Sharma",
                trailingContent = {
                    MapleStatusChip(text = "Waiting")
                },
                onClick = {}
            )
            MapleListRow(
                title = "10 Oct 2026, 16:05",
                subtitle = "Nurse N102",
                trailingContent = {
                    MapleDataChip(text = "BP 128/84")
                },
                onClick = {}
            )
        }
    }
}
