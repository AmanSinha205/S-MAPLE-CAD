package org.smaple.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.smaple.core.designsystem.theme.BannerShape
import org.smaple.core.designsystem.theme.Ink
import org.smaple.core.designsystem.theme.MapleBody
import org.smaple.core.designsystem.theme.MapleLabel
import org.smaple.core.designsystem.theme.MapleRed
import org.smaple.core.designsystem.theme.MapleSpacing
import org.smaple.core.designsystem.theme.MapleTheme
import org.smaple.core.designsystem.theme.RedTint

/**
 * Standard S-MAPLE alert/error banner.
 *
 * Adheres to Section 5:
 * Red tint fill, 18 dp radius, alert icon, one bold line explaining what occurred,
 * and one line explaining what is safe or how to proceed. Never auto-dismisses.
 */
@Composable
fun MapleBanner(
    headline: String,
    safeExplanation: String,
    modifier: Modifier = Modifier,
    actionButton: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = RedTint, shape = BannerShape)
            .padding(all = MapleSpacing.group)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Alert",
                    tint = MapleRed,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(MapleSpacing.small))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = headline,
                        style = MapleLabel,
                        color = Ink
                    )

                    Spacer(modifier = Modifier.height(MapleSpacing.base))

                    Text(
                        text = safeExplanation,
                        style = MapleBody,
                        color = Ink
                    )
                }
            }

            if (actionButton != null) {
                Spacer(modifier = Modifier.height(MapleSpacing.medium))
                actionButton()
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F1)
@Composable
private fun MapleBannerPreview() {
    MapleTheme {
        Column(
            modifier = Modifier
                .padding(MapleSpacing.screenPadding)
                .fillMaxWidth()
        ) {
            MapleBanner(
                headline = "The phone lost touch with the card",
                safeExplanation = "Nothing was changed on the card. Hold your phone flat against the card and tap again."
            )
        }
    }
}
