package week11.stn2002.safestride.service

import android.Manifest
import android.app.Notification
import android.util.Log
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import week11.stn2002.safestride.MainActivity
import week11.stn2002.safestride.R
import week11.stn2002.safestride.data.model.LocationUpdate
import kotlin.math.sqrt

class SafeWalkService : Service(), SensorEventListener {

    private val binder = SafeWalkBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var sensorManager: SensorManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var accelerometer: Sensor? = null

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // State flows for UI
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val _currentLocation = MutableStateFlow<LocationUpdate?>(null)
    val currentLocation: StateFlow<LocationUpdate?> = _currentLocation.asStateFlow()

    private val _fallDetected = MutableStateFlow(false)
    val fallDetected: StateFlow<Boolean> = _fallDetected.asStateFlow()

    private var startTime = 0L
    private var timerRunnable: Runnable? = null
    private val handler = android.os.Handler(Looper.getMainLooper())

    // Fall detection variables
    private var lastAcceleration = 0f
    private var currentAcceleration = 0f
    private var acceleration = 0f
    private val FALL_THRESHOLD = 2f // Very sensitive for emulator testing
    private val IMPACT_THRESHOLD = 12f // Lower threshold for emulator
    private var fallDetectionEnabled = false // delays detection on start to avoid false positives

    companion object {
        const val CHANNEL_ID = "SafeWalkServiceChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_STOP_SERVICE = "STOP_SAFE_WALK"
    }

    inner class SafeWalkBinder : Binder() {
        fun getService(): SafeWalkService = this@SafeWalkService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initializeSensors()
        initializeLocationClient()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Safe Walk Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitoring your safety during Safe Walk"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initializeSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // set baseline values for fall detection
        acceleration = SensorManager.GRAVITY_EARTH
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH
    }

    private fun initializeLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val userId = auth.currentUser?.uid ?: return
                    val locationUpdate = LocationUpdate(
                        userId = userId,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestamp = System.currentTimeMillis(),
                        accuracy = location.accuracy
                    )
                    _currentLocation.value = locationUpdate

                    // Save location to Firestore
                    serviceScope.launch {
                        saveLocationToFirestore(locationUpdate)
                    }
                }
            }
        }
    }

    private fun saveLocationToFirestore(locationUpdate: LocationUpdate) {
        firestore.collection("locationUpdates")
            .add(locationUpdate)
            .addOnSuccessListener { docRef ->
                Log.d("SafeWalkService", "Location saved to Firestore: ${docRef.id}")
            }
            .addOnFailureListener { e ->
                Log.e("SafeWalkService", "Failed to save location to Firestore: ${e.message}")
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopSafeWalk()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, createNotification())
        startSafeWalk()

        return START_STICKY
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, SafeWalkService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Safe Walk Active")
            .setContentText("Monitoring your safety")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(0, "Stop", stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun startSafeWalk() {
        // Reset all state for fresh start
        _isRunning.value = true
        _fallDetected.value = false
        fallDetectionEnabled = false

        // reset acceleration tracking
        lastAcceleration = SensorManager.GRAVITY_EARTH
        currentAcceleration = SensorManager.GRAVITY_EARTH
        acceleration = 0f

        startTime = System.currentTimeMillis()

        // Unregister first in case already registered
        sensorManager.unregisterListener(this)

        // start listening to accelerometer for fall detection
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // start tracking location
        startLocationUpdates()

        // Start timer
        startTimer()

        // Enable fall detection after 3 seconds to ignore initial movement
        handler.removeCallbacksAndMessages(null) // Clear any pending callbacks
        handler.postDelayed({
            fallDetectionEnabled = true
            Log.d("SafeWalkService", "Fall detection now enabled")
        }, 3000)

        Log.d("SafeWalkService", "Safe Walk started - waiting 3 seconds before enabling fall detection")
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // Update every 10 seconds
        ).apply {
            setMinUpdateIntervalMillis(5000L)
            setWaitForAccurateLocation(false)
        }.build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun startTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                if (_isRunning.value) {
                    _elapsedTime.value = System.currentTimeMillis() - startTime
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.post(timerRunnable!!)
    }

    fun stopSafeWalk() {
        _isRunning.value = false

        // Stop sensor listeners
        sensorManager.unregisterListener(this)

        // Stop location updates
        fusedLocationClient.removeLocationUpdates(locationCallback)

        // Stop timer
        timerRunnable?.let { handler.removeCallbacks(it) }

        // Reset state
        _elapsedTime.value = 0L

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun resetFallDetection() {
        _fallDetected.value = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            handleAccelerometerData(event)
        }
    }

    private fun handleAccelerometerData(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        lastAcceleration = currentAcceleration
        currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        val delta = currentAcceleration - lastAcceleration
        acceleration = acceleration * 0.9f + delta

        // Detect sudden acceleration changes (potential fall)
        val accelerationMagnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        // Log sensor data for debugging
        Log.d("SafeWalkService", "Accel: mag=$accelerationMagnitude, delta=${kotlin.math.abs(acceleration)}")

        // Fall detection: check for free fall (low acceleration) followed by impact (high acceleration)
        if (accelerationMagnitude < 3f) {
            Log.d("SafeWalkService", "Potential free fall detected!")
        }

        // Check for impact (sudden high acceleration) - only if detection is enabled
        if (fallDetectionEnabled && (kotlin.math.abs(acceleration) > FALL_THRESHOLD || accelerationMagnitude > IMPACT_THRESHOLD)) {
            Log.w("SafeWalkService", "FALL DETECTED! accel=${kotlin.math.abs(acceleration)}, mag=$accelerationMagnitude")
            if (!_fallDetected.value && _isRunning.value) {
                _fallDetected.value = true
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // required by SensorEventListener but we don't need it
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        sensorManager.unregisterListener(this)
        fusedLocationClient.removeLocationUpdates(locationCallback)
        timerRunnable?.let { handler.removeCallbacks(it) }
    }
}
