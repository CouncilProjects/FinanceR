package com.afterdark.financer.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.afterdark.financer.ui.screens.graphs.GraphsScreen
import com.afterdark.financer.ui.screens.history.HistoryScreen
import com.afterdark.financer.ui.screens.home.HomeScreen
import com.afterdark.financer.ui.screens.profile.ProfileScreen
import kotlinx.serialization.Serializable


@Serializable
object Profile

@Serializable
data class Home(val userId:Long)

@Serializable
data class History(val userId:Long)

@Serializable
data class Graph(val userId:Long)


@Composable
fun FinanceRApp(viewModel : AppViewModel = viewModel(factory = AppViewModel.Factory)) {
    val ui by viewModel.uiState.collectAsState()
    val starting = viewModel.startingValue
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.PROFILE) }
    val navController = rememberNavController()

    //until we read the datastore and see if there is a last visited profile we dont render anything.
    // typed route start destination
    val startDest: Any = if (starting!=null && starting>-1){
        Log.d("NAV","starting is : ${starting}")
        currentDestination = AppDestinations.HOME
        Home(starting)
    } else {
        currentDestination = AppDestinations.PROFILE
        Profile
    }


    when(ui){
        is SimpleUi.Loading -> {
            Scaffold() {innerPadding ->
                Column(modifier = Modifier.padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                    Text(text = "Loading info...")
                }
            }
        }

        is SimpleUi.Done ->{
            NavigationSuiteScaffold(
                navigationSuiteItems = {
                    AppDestinations.entries.forEach { dest ->
                        item(
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                            enabled = dest.label=="Profile" || (ui as SimpleUi.Done).userId>-1,
                            selected = dest == currentDestination,
                            onClick = {
                                currentDestination = dest
                                // navigate using typed routes
                                val routeObj = when (dest) {
                                    AppDestinations.HOME -> Home((ui as SimpleUi.Done).userId)
                                    AppDestinations.PROFILE -> Profile
                                    AppDestinations.HISTORY -> History((ui as SimpleUi.Done).userId)
                                    AppDestinations.GRAPH -> Graph((ui as SimpleUi.Done).userId)
                                }
                                navController.navigate(routeObj)
                            }
                        )
                    }
                }
            ) {
                Scaffold { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        NavHost(navController = navController, startDestination = startDest) {
                            composable<Home> { backStackEntry ->
                                val args = backStackEntry.toRoute<Home>()
                                HomeScreen()
                            }
                            composable<Profile> { backStackEntry ->

                                ProfileScreen()
                            }
                            composable<History> { backStackEntry ->
                                val args = backStackEntry.toRoute<History>()
                                HistoryScreen()
                            }
                            composable<Graph> { backStackEntry ->
                                val args = backStackEntry.toRoute<Graph>()
                                GraphsScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class AppDestinations( // NOTE the AppDestinations here are pure UI, they need to be mapped to the actual typed routes
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    HISTORY("History", Icons.Default.History),
    GRAPH("Graphs",Icons.Default.AutoGraph),
    PROFILE("Profile", Icons.Default.AccountBox), ;

}