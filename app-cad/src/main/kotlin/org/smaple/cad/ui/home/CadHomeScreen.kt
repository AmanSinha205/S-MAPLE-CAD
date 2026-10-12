package org.smaple.cad.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.smaple.core.designsystem.component.MapleButton
import org.smaple.core.designsystem.component.MapleButtonVariant
import org.smaple.core.designsystem.component.MapleCard
import org.smaple.core.designsystem.component.MapleToken
import org.smaple.core.designsystem.theme.MapleBody
import org.smaple.core.designsystem.theme.MapleBodyLarge
import org.smaple.core.designsystem.theme.MapleDimensions
import org.smaple.core.designsystem.theme.MapleHeadline1
import org.smaple.core.designsystem.theme.MapleSpacing
import org.smaple.core.designsystem.theme.MapleTheme
import org.smaple.core.designsystem.theme.MapleTitle
import org.smaple.core.designsystem.theme.Moss
import org.smaple.core.designsystem.theme.Sage

@Composable
fun CadHomeScreen(
    viewModel: CadHomeViewModel,
    onNavigateToScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CadHomeScreenContent(
        uiState = uiState,
        onScanClicked = onNavigateToScan,
        modifier = modifier
    )
}

@Composable
fun CadHomeScreenContent(
    uiState: CadHomeUiState,
    onScanClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Sage),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = MapleDimensions.maxContentWidth)
                .fillMaxSize()
                .padding(MapleSpacing.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.1f))

            MapleToken(
                state = uiState.tokenState,
                size = MapleDimensions.tokenSizeHome
            )

            Spacer(modifier = Modifier.height(MapleSpacing.extraLarge))

            Text(
                text = uiState.headline,
                style = MapleHeadline1,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(MapleSpacing.small))

            Text(
                text = uiState.helperText,
                style = MapleBodyLarge,
                color = Moss,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(0.3f))

            MapleCard {
                Text(
                    text = "CAD Healthcare Terminal",
                    style = MapleTitle
                )
                Spacer(modifier = Modifier.height(MapleSpacing.extraSmall))
                Text(
                    text = "Milestone 0: Architecture baseline initialized. Tap below to test token interaction.",
                    style = MapleBody,
                    color = Moss
                )
            }

            Spacer(modifier = Modifier.weight(0.2f))

            MapleButton(
                text = "Tap to Scan Card",
                onClick = onScanClicked,
                variant = MapleButtonVariant.Primary
            )

            Spacer(modifier = Modifier.height(MapleSpacing.medium))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CadHomeScreenPreview() {
    MapleTheme {
        CadHomeScreenContent(
            uiState = CadHomeUiState(),
            onScanClicked = {}
        )
    }
}
