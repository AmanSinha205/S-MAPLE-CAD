package org.smaple.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.smaple.core.designsystem.theme.AtkinsonHyperlegibleFontFamily
import org.smaple.core.designsystem.theme.Ink
import org.smaple.core.designsystem.theme.InputFieldShape
import org.smaple.core.designsystem.theme.MapleCaption
import org.smaple.core.designsystem.theme.MapleDimensions
import org.smaple.core.designsystem.theme.MapleLabel
import org.smaple.core.designsystem.theme.MapleRed
import org.smaple.core.designsystem.theme.MapleSpacing
import org.smaple.core.designsystem.theme.MapleTheme
import org.smaple.core.designsystem.theme.Mist2
import org.smaple.core.designsystem.theme.Moss
import org.smaple.core.designsystem.theme.Pine
import org.smaple.core.designsystem.theme.White

/**
 * Standard S-MAPLE accessible input field.
 *
 * Implements visible field labels above inputs (never placeholder-only),
 * 2 dp Mist 2 border, 16 dp radius, minimum 56 dp field height, and 24 sp bold value text.
 */
@Composable
fun MapleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    errorMessage: String? = null,
    isMultiLine: Boolean = false,
    enabled: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val fieldMinHeight = if (isMultiLine) {
        MapleDimensions.notesBoxHeight
    } else {
        MapleDimensions.inputFieldHeight
    }

    val valueTextStyle = TextStyle(
        fontFamily = AtkinsonHyperlegibleFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        color = if (enabled) Ink else Moss
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Visible label above the field
        Text(
            text = label,
            style = MapleLabel,
            color = if (errorMessage != null) MapleRed else Ink,
            modifier = Modifier.padding(bottom = MapleSpacing.extraSmall)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = fieldMinHeight)
                .background(color = White, shape = InputFieldShape)
                .border(
                    width = MapleDimensions.inputBorderWidth,
                    color = if (errorMessage != null) MapleRed else Mist2,
                    shape = InputFieldShape
                )
                .padding(horizontal = MapleSpacing.medium, vertical = MapleSpacing.small),
            contentAlignment = if (isMultiLine) Alignment.TopStart else Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = if (isMultiLine) Alignment.Top else Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = if (isMultiLine) Alignment.TopStart else Alignment.CenterStart
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            style = valueTextStyle.copy(color = Moss, fontWeight = FontWeight.Normal)
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        enabled = enabled,
                        textStyle = valueTextStyle,
                        cursorBrush = SolidColor(Pine),
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        singleLine = !isMultiLine,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (trailingContent != null) {
                    Spacer(modifier = Modifier.padding(start = MapleSpacing.small))
                    trailingContent()
                }
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(MapleSpacing.base))
            Text(
                text = errorMessage,
                style = MapleCaption,
                color = MapleRed,
                modifier = Modifier.padding(horizontal = MapleSpacing.small)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F1)
@Composable
private fun MapleTextFieldPreview() {
    MapleTheme {
        Column(
            modifier = Modifier
                .padding(MapleSpacing.screenPadding)
                .fillMaxWidth()
        ) {
            MapleTextField(
                value = "120",
                onValueChange = {},
                label = "Systolic blood pressure (mmHg)"
            )
            Spacer(modifier = Modifier.height(MapleSpacing.medium))
            MapleTextField(
                value = "",
                placeholder = "Tap to add doctor note...",
                onValueChange = {},
                label = "Clinical notes",
                isMultiLine = true
            )
            Spacer(modifier = Modifier.height(MapleSpacing.medium))
            MapleTextField(
                value = "300",
                onValueChange = {},
                label = "Heart rate (bpm)",
                errorMessage = "Heart rate must be between 0 and 255 bpm"
            )
        }
    }
}
