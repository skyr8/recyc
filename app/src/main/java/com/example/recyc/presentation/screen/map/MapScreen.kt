package com.example.recyc.presentation.screen.map

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.recyc.utils.Logger
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.concurrent.TimeUnit

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    onPositionSaved: () -> Unit,
    onBackPressed: () -> Unit,
    viewModel: MapViewModel = hiltViewModel(),
    fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(LocalContext.current)
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_LOW_POWER, TimeUnit.MINUTES.toMillis(1))
                .apply {
                    setMinUpdateDistanceMeters(1.0f)
                    setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                    setWaitForAccurateLocation(true)
                }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                Logger.log("MapScreen:::", "retrieve location")

                locationResult.lastLocation?.let { location ->
                    Logger.log("MapScreen:::", "lastLocation: $location")

                    val latLng = LatLng(location.latitude, location.longitude)
                    viewModel.updateUserLocation(latLng)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, context.mainLooper)
    }
    val uiState by viewModel.uiState.collectAsState()
    val homPosition = uiState.homeLocation
    val selectedPosition = uiState.selectedPosition
    val userLocation = uiState.userLocation
    Logger.log("MapScreen:::", "userLocation: $userLocation")

    MapScreenContent(
        onSavePosition = { latLng ->
            viewModel.saveGeofenceLocation(latLng)
            onPositionSaved()
        },
        userLocation = userLocation,
        onBackPressed = onBackPressed,
        homePosition = homPosition,
        onSelectedPositionChanged = { latLng ->
            viewModel.updateSelectedPosition(latLng)
        },
        selectedPosition = selectedPosition,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapScreenContent(
    onSavePosition: (LatLng) -> Unit,
    userLocation: LatLng?,
    isPreview: Boolean = false,
    homePosition: LatLng?,
    onBackPressed: () -> Unit = {},
    onSelectedPositionChanged: (LatLng) -> Unit = {},
    selectedPosition: LatLng? = null
) {
    val mapProperties = remember { mutableStateOf(MapProperties()) }
    val mapUiSettings = remember { mutableStateOf(MapUiSettings()) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(
                homePosition?.latitude ?: (userLocation?.latitude ?: 0.0),
                homePosition?.longitude ?: (userLocation?.longitude ?: 0.0)
            ), 17f
        )
    }
    cameraPositionState.position = CameraPosition.fromLatLngZoom(
        LatLng(
            homePosition?.latitude ?: (userLocation?.latitude ?: 0.0),
            homePosition?.longitude ?: (userLocation?.longitude ?: 0.0)
        ), 17f
    )

    Scaffold(topBar = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "",
                modifier = Modifier
                    .size(38.dp)
                    .clickable { onBackPressed() }
            )
            Text(
                text = "Select your home location",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(top = 54.dp)
        ) {
            if (isPreview) {
                Box(modifier = Modifier.weight(1f)) {
                    Image(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Default Map Icon",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                GoogleMap(
                    modifier = Modifier.weight(1f),
                    properties = mapProperties.value,
                    uiSettings = mapUiSettings.value,
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        onSelectedPositionChanged(latLng)
                    }
                ) {
                    selectedPosition?.let {
                        Marker(
                            title = "Selected Position",
                            state = MarkerState(position = it)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    selectedPosition?.let { latLng ->
                        onSavePosition(latLng)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = selectedPosition != null
            ) {
                Text("Save Position")
            }
        }
    }
}

@Composable
@Preview
fun MapScreenPreview() {
    MapScreenContent(
        onSavePosition = {},
        userLocation = LatLng(0.0, 0.0),
        isPreview = true,
        homePosition = null
    )
}
