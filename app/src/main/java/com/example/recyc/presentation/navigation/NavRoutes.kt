package com.example.recyc.presentation.navigation

import android.location.Location
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.recyc.SharedViewModel
import com.example.recyc.presentation.screen.edit.DetailScreen
import com.example.recyc.presentation.screen.home.HomeScreen
import com.example.recyc.presentation.screen.map.MapScreen
import com.example.recyc.utils.Logger

object Routes {
    const val RecyclingScreen = "recycling_screen"
    const val DetailScreen = "detail_screen/{dayId}"
    const val MapScreen = "map_screen"
    fun createDetailRoute(dayId: Int) = "detail_screen/$dayId"
}

@Composable
fun Navigator(
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    userLocation: Location?,
    onItemSaved: () -> Unit = {}
) {
    Logger.log("Navigator:::", "userLocation: $userLocation")
    NavHost(navController = navController, startDestination = Routes.RecyclingScreen) {
        composable(Routes.RecyclingScreen) {
            HomeScreen(onItemClick = {
                navController.navigate(Routes.createDetailRoute(it))

            }, onSettingsClick = {
                navController.navigate(Routes.MapScreen)
            }, sharedViewModel = sharedViewModel)
        }
        composable(Routes.DetailScreen) { backStackEntry ->
            val dayId = backStackEntry.arguments?.getString("dayId")?.toIntOrNull()
            dayId?.let {
                DetailScreen(id = dayId, onBackPressed = {
                    navController.popBackStack()
                }, onSaveChanges = {
                    navController.navigate(Routes.RecyclingScreen) {
                        popUpTo(Routes.RecyclingScreen) { inclusive = true }
                        onItemSaved()
                    }
                })
            }
        }
        composable(Routes.MapScreen) {
            MapScreen(
                onPositionSaved = {
                    navController.navigate(Routes.RecyclingScreen) {
                        popUpTo(Routes.RecyclingScreen) { inclusive = true }
                        onItemSaved()
                    }
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}
