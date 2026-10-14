package org.smaple.cad.ui.patient

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.smaple.cad.data.model.Patient
import org.smaple.core.designsystem.component.MapleButton
import org.smaple.core.designsystem.component.MapleButtonVariant
import org.smaple.core.designsystem.component.MapleCard
import org.smaple.core.designsystem.theme.MapleBodyLarge
import org.smaple.core.designsystem.theme.MapleHeadline2
import org.smaple.core.designsystem.theme.MapleSpacing
import org.smaple.core.designsystem.theme.MapleTheme

@Composable
fun PatientFoundScreen(
    viewModel: PatientFoundViewModel,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val patient by viewModel.patient.collectAsStateWithLifecycle()

    // Ensure patient data is loaded; if not, show nothing (could show loading)
    LaunchedEffect(patient) {
        // No extra action needed; UI reacts automatically.
    }

    Column(
        modifier = modifier
            .padding(MapleSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(MapleSpacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MapleCard {
            if (patient != null) {
                PatientInfo(patient!!)
            } else {
                Text(text = "Loading...", style = MapleBodyLarge)
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MapleButton(
                text = "View Full Profile",
                onClick = { /* TODO: implement navigation */ },
                variant = MapleButtonVariant.Primary,
                enabled = patient != null
            )
            MapleButton(
                text = "Back to Home",
                onClick = onBackToHome,
                variant = MapleButtonVariant.Primary
            )
        }
    }
}

@Composable
private fun PatientInfo(patient: Patient) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = patient.name, style = MapleHeadline2)
        Row {
            Text(text = "ID: ", style = MapleBodyLarge)
            Text(text = patient.patientId, style = MapleBodyLarge)
        }
        Row {
            Text(text = "Age: ", style = MapleBodyLarge)
            Text(text = "${patient.age}", style = MapleBodyLarge)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Gender: ", style = MapleBodyLarge)
            Text(text = patient.gender.name, style = MapleBodyLarge)
        }
        Row {
            Text(text = "Blood Group: ", style = MapleBodyLarge)
            Text(text = patient.riskStatus.name, style = MapleBodyLarge) // placeholder; replace if needed
        }
        Row {
            Text(text = "Last Visit: ", style = MapleBodyLarge)
            Text(text = patient.lastVisit.toString(), style = MapleBodyLarge)
        }
        Text(text = "Summary: ${patient.medicalSummary}", style = MapleBodyLarge)
        Text(text = "Risk: ${patient.riskStatus}", style = MapleBodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun PatientFoundScreenPreview() {
    MapleTheme {
        // Preview with mock patient
        val mock = Patient(
            patientId = "PT-100234",
            name = "John Smith",
            age = 36,
            gender = org.smaple.core.model.Gender.MALE,
            lastVisit = java.time.LocalDateTime.now(),
            medicalSummary = "Hypertension, Seasonal Allergies",
            riskStatus = Patient.RiskStatus.MEDIUM
        )
        PatientInfo(mock)
    }
}
