package org.smaple.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.smaple.core.designsystem.theme.Ink
import org.smaple.core.designsystem.theme.MapleCaption
import org.smaple.core.designsystem.theme.MapleDimensions
import org.smaple.core.designsystem.theme.MapleLabel
import org.smaple.core.designsystem.theme.MapleSpacing
import org.smaple.core.designsystem.theme.MapleTheme
import org.smaple.core.designsystem.theme.Mist2
import org.smaple.core.designsystem.theme.Moss
import org.smaple.core.designsystem.theme.PillShape
import org.smaple.core.designsystem.theme.Pine
import org.smaple.core.designsystem.theme.White

enum class MapleButtonVariant {
    Primary,
    PrimaryFlow,
    Secondary,
    Amber,
    Text
}

/**
 * Standard S-MAPLE accessible button component.
 *
 * Enforces minimum touch target heights, pill geometry, and accessible color contrast.
 * When disabled, a [disabledReason] can be provided to clearly explain why the action cannot proceed.
 */
@Composable
fun MapleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: MapleButtonVariant = MapleButtonVariant.Primary,
    enabled: Boolean = true,
    disabledReason: String? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (variant) {
            MapleButtonVariant.Primary, MapleButtonVariant.PrimaryFlow -> {
                val minHeight = if (variant == MapleButtonVariant.PrimaryFlow) {
                    MapleDimensions.primaryButtonFlowHeight
                } else {
                    MapleDimensions.primaryButtonHeight
                }

                Button(
                    onClick = onClick,
                    enabled = enabled,
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Pine,
                        contentColor = White,
                        disabledContainerColor = Mist2,
                        disabledContentColor = Moss
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = minHeight)
                ) {
                    Text(text = text, style = MapleLabel)
                }
            }

            MapleButtonVariant.Secondary -> {
                OutlinedButton(
                    onClick = onClick,
                    enabled = enabled,
                    shape = PillShape,
                    border = BorderStroke(
                        width = 2.dp,
                        color = if (enabled) Pine else Mist2
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Pine,
                        disabledContentColor = Moss
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = MapleDimensions.secondaryButtonHeight)
                ) {
                    Text(text = text, style = MapleLabel)
                }
            }

            MapleButtonVariant.Amber -> {
                Button(
                    onClick = onClick,
                    enabled = enabled,
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MapleTheme.extendedColors.mapleAmber,
                        contentColor = Ink,
                        disabledContainerColor = Mist2,
                        disabledContentColor = Moss
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = MapleDimensions.amberButtonHeight)
                ) {
                    Text(text = text, style = MapleLabel)
                }
            }

            MapleButtonVariant.Text -> {
                TextButton(
                    onClick = onClick,
                    enabled = enabled,
                    shape = PillShape,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Pine,
                        disabledContentColor = Moss
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = MapleDimensions.textButtonHeight)
                ) {
                    Text(text = text, style = MapleLabel)
                }
            }
        }

        if (!enabled && !disabledReason.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(MapleSpacing.extraSmall))
            Text(
                text = disabledReason,
                style = MapleCaption,
                color = Moss,
                modifier = Modifier.padding(horizontal = MapleSpacing.small)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F1)
@Composable
private fun MapleButtonPreview() {
    MapleTheme {
        Column(
            modifier = Modifier
                .padding(MapleSpacing.screenPadding)
                .fillMaxWidth()
        ) {
            MapleButton(
                text = "Primary action",
                onClick = {},
                variant = MapleButtonVariant.Primary
            )
            Spacer(modifier = Modifier.height(MapleSpacing.medium))
            MapleButton(
                text = "Secondary action",
                onClick = {},
                variant = MapleButtonVariant.Secondary
            )
            Spacer(modifier = Modifier.height(MapleSpacing.medium))
            MapleButton(
                text = "Upload notes",
                onClick = {},
                variant = MapleButtonVariant.Amber
            )
            Spacer(modifier = Modifier.height(MapleSpacing.medium))
            MapleButton(
                text = "Scan a different patient",
                onClick = {},
                variant = MapleButtonVariant.Text
            )
            Spacer(modifier = Modifier.height(MapleSpacing.medium))
            MapleButton(
                text = "Save record",
                onClick = {},
                enabled = false,
                disabledReason = "Tap a patient card first to enable saving"
            )
        }
    }
}
