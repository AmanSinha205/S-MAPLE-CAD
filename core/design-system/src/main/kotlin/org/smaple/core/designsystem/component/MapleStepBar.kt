package org.smaple.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.smaple.core.designsystem.theme.MapleCaption
import org.smaple.core.designsystem.theme.MapleDimensions
import org.smaple.core.designsystem.theme.MapleSpacing
import org.smaple.core.designsystem.theme.MapleTheme
import org.smaple.core.designsystem.theme.Mist2
import org.smaple.core.designsystem.theme.Moss
import org.smaple.core.designsystem.theme.Pine

/**
 * Step bar for multi-step flows (Section 5).
 * Three segments (e.g. Scan, Vitals, Save), 8 dp tall.
 * Done and current segments are Pine, remaining are Mist 2.
 * The current step label is bold.
 */
@Composable
fun MapleStepBar(
    steps: List<String>,
    currentStepIndex: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MapleSpacing.screenPadding)
    ) {
        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, label ->
                val isCurrent = index == currentStepIndex
                val isDone = index < currentStepIndex
                val color = if (isCurrent || isDone) Pine else Moss
                val weight = if (isCurrent) FontWeight.Bold else FontWeight.Normal

                Text(
                    text = label,
                    style = MapleCaption,
                    color = color,
                    fontWeight = weight
                )
            }
        }

        Spacer(modifier = Modifier.height(MapleSpacing.extraSmall))

        // Progress bar segments
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            steps.forEachIndexed { index, _ ->
                val isDoneOrCurrent = index <= currentStepIndex
                val segmentColor = if (isDoneOrCurrent) Pine else Mist2

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(MapleDimensions.stepBarHeight)
                        .clip(RoundedCornerShape(4.dp))
                        .background(segmentColor)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F1)
@Composable
private fun MapleStepBarPreview() {
    MapleTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MapleStepBar(
                steps = listOf("Scan", "Vitals", "Save"),
                currentStepIndex = 1
            )
        }
    }
}
