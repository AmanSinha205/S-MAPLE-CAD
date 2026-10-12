package org.smaple.cad.ui.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.smaple.core.designsystem.component.MapleButton
import org.smaple.core.designsystem.component.MapleButtonVariant
import org.smaple.core.designsystem.component.MapleToken
import org.smaple.core.designsystem.theme.MapleBodyLarge
import org.smaple.core.designsystem.theme.MapleHeadline2
import org.smaple.core.designsystem.theme.MapleSpacing
import org.smaple.core.designsystem.theme.MapleTokenState
import org.smaple.core.designsystem.theme.MapleTheme

@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    onNavigateToPatientFound: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val patient by viewModel.patient.collectAsStateWithLifecycle()

    // When a patient is discovered, navigate
    LaunchedEffect(patient) {
        if (patient != null) {
            onNavigateToPatientFound()
        }
    }

    Column(
        modifier = modifier
            .padding(MapleSpacing.screenPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MapleToken(
            state = when (uiState) {
                ScanUiState.Idle -> MapleTokenState.Idle
                ScanUiState.Scanning -> MapleTokenState.Scanning
            },
            size = MapleDimensions.tokenSizeHome
        )
        Spacer(modifier = Modifier.height(MapleSpacing.extraLarge))
        Text(
            text = "Hold your phone against the patient's phone or card.",
            style = MapleBodyLarge,
            color = MapleHeadline2
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (uiState is ScanUiState.Idle) {
            MapleButton(
                text = "Start Scan",
                onClick = { viewModel.startScanAndDiscover() },
                variant = MapleButtonVariant.Primary
            )
        } else {
            MapleButton(
                text = "Cancel",
                onClick = { viewModel.cancelScan() },
                variant = MapleButtonVariant.Tertiary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScanScreenPreview() {
    MapleTheme {
        // Provide dummy ViewModel for preview
        // In preview we can't instantiate real ViewModel, so just omit UI content.
    }
}
