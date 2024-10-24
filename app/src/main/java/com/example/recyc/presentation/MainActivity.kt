package com.example.recyc.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation.compose.rememberNavController
import com.example.recyc.domain.usecase.RunPeriodicWorkUseCase
import com.example.recyc.presentation.navigation.Navigator
import com.example.recyc.presentation.screen.recycling.RecyclingScreen
import com.example.recyc.presentation.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var runPeriodicWorkUseCase: RunPeriodicWorkUseCase

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runPeriodicWorkUseCase()
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()
            val hapticFeedback = LocalHapticFeedback.current

            AppTheme(isDynamicColor = true) {
                Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it),
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                    ) {
                        val navController = rememberNavController()
                        Navigator(navController = navController, onItemSaved = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Changes successfully saved")
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        })
                    }
                }
            }
        }
    }
}
