package com.example.recyc.domain.geofence

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.recyc.R
import com.example.recyc.domain.mapper.toIcon
import com.example.recyc.domain.usecase.GetCurrentRecyclerDayUseCase
import com.example.recyc.domain.usecase.PreferenceUseCase
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
    private var lastNotificationTime: Long = 0

    @Inject
    lateinit var getRecyclerUseCase: GetCurrentRecyclerDayUseCase

    @Inject
    lateinit var preferenceUseCase: PreferenceUseCase

    override fun onCreate() {
        super.onCreate()
        Log.d("LOCATION_SERVICE:::", "onCreate called")
        createNotificationChannel()
        startForegroundService()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        startLocationMonitoring()
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
                NotificationManager.IMPORTANCE_MAX
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

    private fun startLocationMonitoring() {
        Log.d("LOCATION_SERVICE:::", "startLocationMonitoring called")
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
        Log.d("LOCATION_SERVICE:::", "onStartCommand called")
        return START_STICKY
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Log.d(
                "LOCATION_SERVICE:::",
                "onLocationResult called with locations: ${locationResult.locations}"
            )
            locationResult.locations.forEach { location ->
                checkUserDistanceFromHome(location)
            }
        }
    }

    private fun checkUserDistanceFromHome(location: Location) {
        Log.d("LOCATION_SERVICE:::", "checkUserDistanceFromHome called with location: $location")
        val homeLocation = Location("home").apply {
            latitude = homeLatitude
            longitude = homeLongitude
        }

        val distance = location.distanceTo(homeLocation)
        Log.d("LOCATION_SERVICE:::", "Distance from home: $distance meters")
        if (distance > homeRadius) {
            Log.d("LOCATION_SERVICE:::", "User is outside the home radius")
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastNotificationTime >= 3600000) { // 1 hour in milliseconds
                sendNotification()
                lastNotificationTime = currentTime
            } else {
                Log.d("LOCATION_SERVICE:::", "Notification already sent within the last hour")
            }
        } else {
            Log.d("LOCATION_SERVICE:::", "User is within the home radius")
        }
    }

    private fun sendNotification() {
        Log.d("LOCATION_SERVICE:::", "sendNotification called")
        CoroutineScope(Dispatchers.Default).launch {
            sendNotification(this@LocationService)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("LOCATION_SERVICE:::", "onBind called")
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LOCATION_SERVICE:::", "onDestroy called")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private suspend fun sendNotification(context: Context) {
        val dayModel = getRecyclerUseCase()
        Log.d("LOCATION_SERVICE:::", "sendNotification (context) called")
        val channelId = "geofence_channel"
        val notificationId = 2

        val confirmIntent = Intent(context, GeofenceActionReceiver::class.java).apply {
            action = "com.example.recyc.ACTION_CONFIRM_DAY"
            putExtra("date", dayModel?.day?.name)
            putExtra("notificationId", notificationId)
        }

        val confirmPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            confirmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        dayModel?.type?.first()?.toIcon()?.let { icon ->
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(icon)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("Do you recycle ${dayModel.type.joinToString(", ")} today?")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(longArrayOf(0, 500, 1000))
                .setDefaults(Notification.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .addAction(
                    R.drawable.ic_launcher_foreground,
                    "Yes",
                    confirmPendingIntent
                )

            val notification = builder.build()
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, notification)
            }
        }
    }
}