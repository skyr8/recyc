package com.example.recyc.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.recyc.utils.Logger
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var runPeriodicWorkUseCase: RunPeriodicWorkUseCase

    private val sharedViewModel: SharedViewModel by viewModels()

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val _locationFlow = MutableStateFlow<Location?>(null)
    val locationFlow: StateFlow<Location?> = _locationFlow.asStateFlow()

    private val confirmationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val date: Boolean = intent?.getBooleanExtra("isConfirmed", false) ?: return
            sharedViewModel.setDayConfirmation(date)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.all { it.value }) {
                startLocationService()
                runPeriodicWorkUseCase()
                setupLocationListener()
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
            val location by locationFlow.collectAsState()

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
                            userLocation = location,
                            onItemSaved = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Changes successfully saved")
                                }
                            })
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupLocationListener() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            Logger.log("MainActivity:::", "Location: $location")
            location?.let {
                _locationFlow.update { it }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
            runPeriodicWorkUseCase()
            setupLocationListener()
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
