package com.example.recyc.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.recyc.presentation.screen.edit.DetailScreen
import com.example.recyc.presentation.screen.recycling.RecyclingScreen

object Routes {
    const val RecyclingScreen = "recycling_screen"
    const val DetailScreen = "detail_screen/{dayId}"
    fun createDetailRoute(dayId: Int) = "detail_screen/$dayId"
}

@Composable
fun Navigator(navController: NavHostController, onItemSaved: () -> Unit = {}) {
    NavHost(navController = navController, startDestination = Routes.RecyclingScreen) {
        composable(Routes.RecyclingScreen) {
            RecyclingScreen(onItemClick = {
                navController.navigate(Routes.createDetailRoute(it))

            })
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
    }
}