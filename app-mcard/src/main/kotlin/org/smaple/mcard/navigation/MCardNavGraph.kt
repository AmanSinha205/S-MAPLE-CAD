package org.smaple.mcard.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.smaple.mcard.ui.home.MCardHomeScreen
import org.smaple.mcard.ui.home.MCardHomeViewModel

object MCardDestinations {
    const val HOME_ROUTE = "home"
}

@Composable
fun MCardNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = MCardDestinations.HOME_ROUTE
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(route = MCardDestinations.HOME_ROUTE) {
            val viewModel: MCardHomeViewModel = hiltViewModel()
            MCardHomeScreen(viewModel = viewModel)
        }
    }
}
