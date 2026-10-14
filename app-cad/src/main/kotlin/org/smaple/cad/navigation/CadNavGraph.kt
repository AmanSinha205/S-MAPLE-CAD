package org.smaple.cad.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.smaple.cad.ui.home.CadHomeScreen
import org.smaple.cad.ui.home.CadHomeViewModel
import org.smaple.cad.ui.scan.ScanScreen
import org.smaple.cad.ui.scan.ScanViewModel
import org.smaple.cad.ui.patient.PatientFoundScreen
import org.smaple.cad.ui.patient.PatientFoundViewModel

object CadDestinations {
    const val HOME_ROUTE = "home"
    const val SCAN_ROUTE = "scan"
    const val PATIENT_FOUND_ROUTE = "patient_found"
}

/**
 * Navigation host for the CAD application.
 */
@Composable
fun CadNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = CadDestinations.HOME_ROUTE,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(route = CadDestinations.HOME_ROUTE) {
            val viewModel: CadHomeViewModel = hiltViewModel()
            CadHomeScreen(viewModel = viewModel, onNavigateToScan = {
                navController.navigate(CadDestinations.SCAN_ROUTE)
            })
        }
        composable(route = CadDestinations.SCAN_ROUTE) {
            val viewModel: ScanViewModel = hiltViewModel()
            ScanScreen(viewModel = viewModel, onNavigateToPatientFound = {
                navController.navigate(CadDestinations.PATIENT_FOUND_ROUTE)
            })
        }
        composable(route = CadDestinations.PATIENT_FOUND_ROUTE) {
            val viewModel: PatientFoundViewModel = hiltViewModel()
            PatientFoundScreen(
                viewModel = viewModel,
                onBackToHome = {
                    navController.popBackStack(
                        CadDestinations.HOME_ROUTE,
                        inclusive = false
                    )
                }
            )
        }
    }
}
