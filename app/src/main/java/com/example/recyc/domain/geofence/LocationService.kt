package com.example.recyc.domain.geofence

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.recyc.R
import com.example.recyc.domain.usecase.GetCurrentDayUseCase
import com.example.recyc.domain.usecase.GetCurrentRecyclerDayUseCase
import com.example.recyc.domain.usecase.PreferenceUseCase
import com.example.recyc.utils.Logger
import com.example.recyc.utils.isHoursBefore
import com.example.recyc.utils.showNotification
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient
    private val homeLatitude = 38.121020
    private val homeLongitude = 13.355050
    private val homeRadius = 50.0f
    private val maxDistance = 1000.0f
    private var lastNotificationTime: Long = 0

    @Inject
    lateinit var getRecyclerUseCase: GetCurrentRecyclerDayUseCase

    @Inject
    lateinit var preferenceUseCase: PreferenceUseCase

    @Inject
    lateinit var currentDayUseCase: GetCurrentDayUseCase

    override fun onCreate() {
        super.onCreate()

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val foregroundServiceChannel = NotificationChannel(
                "foreground_service_channel",
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val geofenceChannel = NotificationChannel(
                "geofence_channel",
                "Geofence Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            geofenceChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(foregroundServiceChannel)
            manager.createNotificationChannel(geofenceChannel)
        }
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, "foreground_service_channel")
            .setContentTitle("Location Service")
            .setContentText("Monitoring location in the background")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        startForeground(1, notification)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationMonitoring() {
        Logger.log("LOCATION_SERVICE:::", "startLocationMonitoring called")
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_LOW_POWER, TimeUnit.MINUTES.toMillis(1))
                .apply {
                    setMinUpdateDistanceMeters(1.0f)
                    setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                    setWaitForAccurateLocation(true)
                }.build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.log("LOCATION_SERVICE:::", "onStartCommand called")
        preferenceUseCase.setServiceUp(true)
        Logger.log("LOCATION_SERVICE:::", "onCreate called")
        createNotificationChannel()
        startForegroundService()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        startLocationMonitoring()
        return START_STICKY
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Logger.log(
                "LOCATION_SERVICE:::",
                "onLocationResult called with locations: ${locationResult.locations}"
            )
            locationResult.locations.forEach { location ->
                checkUserDistanceFromHome(location)
            }
        }
    }

    private fun checkUserDistanceFromHome(location: Location) {
        if(preferenceUseCase.isDaySkipped(currentDayUseCase())) return
        if (preferenceUseCase.isCurrentDayDone(currentDayUseCase())) return
        val userLocation = preferenceUseCase.getHomeLocation()
        Logger.log("LOCATION_SERVICE:::", "checkUserDistanceFromHome called with location: $location")
        val homeLocation = Location("home").apply {
            latitude = userLocation?.latitude ?: homeLatitude
            longitude = userLocation?.longitude ?: homeLongitude
        }

        val distance = location.distanceTo(homeLocation)
        Logger.log("LOCATION_SERVICE:::", "Distance from home: $distance meters")
        if ((distance > homeRadius) && distance < maxDistance) {
            Logger.log("LOCATION_SERVICE:::", "User is outside the home radius")
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastNotificationTime >= 3600000) { // 1 hour in milliseconds
                sendNotification()
                lastNotificationTime = currentTime
            } else {
                Logger.log("LOCATION_SERVICE:::", "Notification already sent within the last hour")
            }
        } else {
            Logger.log("LOCATION_SERVICE:::", "User is within the home radius")
        }
    }

    private fun sendNotification() {
        Logger.log("LOCATION_SERVICE:::", "sendNotification called")
        CoroutineScope(Dispatchers.Default).launch {
            if(isHoursBefore(getRecyclerUseCase()?.hour,3)){
                sendNotification(this@LocationService)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Logger.log("LOCATION_SERVICE:::", "onBind called")
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceUseCase.setServiceUp(false)
        Logger.log("LOCATION_SERVICE:::", "onDestroy called")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private suspend fun sendNotification(context: Context) {
        val dayModel = getRecyclerUseCase()
        dayModel?.let { context.showNotification(it) }
        Logger.log("LOCATION_SERVICE:::", "sendNotification (context) called")
    }
}