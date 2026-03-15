package com.afterdark.financer.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.afterdark.financer.ui.screens.graphs.GraphsScreen
import com.afterdark.financer.ui.screens.history.HistoryScreen
import com.afterdark.financer.ui.screens.home.HomeScreen
import com.afterdark.financer.ui.screens.profile.ProfileScreen
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


@Serializable
object Profile

@Serializable
data class Home(val userId:Long)

@Serializable
data class History(val userId:Long)

@Serializable
data class Graph(val userId:Long)


@SuppressLint("RestrictedApi")
@OptIn(ExperimentalSerializationApi::class)
@Composable
fun FinanceRApp(viewModel : AppViewModel = viewModel(factory = AppViewModel.Factory)) {
    val ui by viewModel.uiState.collectAsState()
    val starting = viewModel.startingValue

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()


    //until we read the datastore and see if there is a last visited profile we don't render anything.
    // typed route start destination
    val startDest: Any = if (starting!=null && starting>-1){
        Home(starting)
    } else {
        Profile
    }


    when(ui){
        is SimpleUi.Loading -> {
            Scaffold {innerPadding ->
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
                            selected = dest.isTheActiveRoute(backStackEntry),
                            onClick = {
                                if(dest.isTheActiveRoute(backStackEntry)) return@item //don't let user double visit
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
                                backStackEntry.toRoute<Home>()
                                HomeScreen()
                            }
                            composable<Profile> { backStackEntry ->

                                ProfileScreen()
                            }
                            composable<History> { backStackEntry ->
                                backStackEntry.toRoute<History>()
                                HistoryScreen()
                            }
                            composable<Graph> { backStackEntry ->
                                backStackEntry.toRoute<Graph>()
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
    val typedRoute: KClass<*>
) {
    HOME("Home", Icons.Default.Home, Home::class),
    HISTORY("History", Icons.Default.History, History::class),
    GRAPH("Graphs",Icons.Default.AutoGraph, Graph::class),
    PROFILE("Profile", Icons.Default.AccountBox, Profile::class)
    ;

    fun isTheActiveRoute(backStack: NavBackStackEntry?) : Boolean{
        return backStack?.destination?.hasRoute(typedRoute) == true
    }

}