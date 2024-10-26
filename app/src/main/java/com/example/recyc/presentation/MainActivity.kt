package com.example.recyc.presentation

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.recyc.SharedViewModel
import com.example.recyc.domain.geofence.LocationService
import com.example.recyc.domain.usecase.RunPeriodicWorkUseCase
import com.example.recyc.presentation.navigation.Navigator
import com.example.recyc.presentation.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var runPeriodicWorkUseCase: RunPeriodicWorkUseCase

    private val sharedViewModel: SharedViewModel by viewModels()

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    private val confirmationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val date: Boolean = intent?.getBooleanExtra("isConfirmed",false) ?: return
            sharedViewModel.setDayConfirmation(date)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.all { it.value }) {
                startLocationService()
                runPeriodicWorkUseCase()
            }
        }
        checkAndRequestPermissions()

        registerReceiver(
            confirmationReceiver,
            IntentFilter("com.example.recyc.ACTION_CONFIRM_DAY_GLOBAL"),
            RECEIVER_EXPORTED
        )

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
                        Navigator(
                            navController = navController,
                            sharedViewModel = sharedViewModel,
                            onItemSaved = {
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

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            startLocationService()
        }
    }

    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(confirmationReceiver)
    }
}
