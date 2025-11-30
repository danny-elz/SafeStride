package week11.stn2002.safestride.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import week11.stn2002.safestride.data.model.LocationUpdate
import week11.stn2002.safestride.service.SafeWalkService

class SafeWalkViewModel(application: Application) : AndroidViewModel(application) {

    private var safeWalkService: SafeWalkService? = null
    private var isBound = false
    private var boundContext: Context? = null  // keeps track of which context we used to bind the service

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val _currentLocation = MutableStateFlow<LocationUpdate?>(null)
    val currentLocation: StateFlow<LocationUpdate?> = _currentLocation.asStateFlow()

    private val _fallDetected = MutableStateFlow(false)
    val fallDetected: StateFlow<Boolean> = _fallDetected.asStateFlow()

    private var startTime = 0L
    private var timerJob: Job? = null
    private var servicePollingJob: Job? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SafeWalkService.SafeWalkBinder
            safeWalkService = binder.getService()
            isBound = true
            Log.d("SafeWalkViewModel", "Service connected and bound")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            safeWalkService = null
            isBound = false
            Log.d("SafeWalkViewModel", "Service disconnected")
        }
    }

    fun startSafeWalk(context: Context) {
        Log.d("SafeWalkViewModel", "Starting Safe Walk session")

        // use app context so the service doesn't get killed when the activity changes
        val appContext = context.applicationContext
        boundContext = appContext

        val intent = Intent(appContext, SafeWalkService::class.java)
        appContext.startForegroundService(intent)
        appContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        _isRunning.value = true
        _fallDetected.value = false
        startTime = System.currentTimeMillis()

        // kick off the timer that tracks how long the walk has been going
        startTimer()

        // start checking the service every 300ms for fall detection updates
        startServicePolling()
        Log.d("SafeWalkViewModel", "Safe Walk session started - timer and polling active")
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isRunning.value) {
                _elapsedTime.value = System.currentTimeMillis() - startTime
                delay(1000)
            }
        }
    }

    private fun startServicePolling() {
        servicePollingJob?.cancel()
        servicePollingJob = viewModelScope.launch {
            while (_isRunning.value) {
                safeWalkService?.let { service ->
                    _currentLocation.value = service.currentLocation.value
                    if (service.fallDetected.value && !_fallDetected.value) {
                        Log.w("SafeWalkViewModel", "FALL DETECTED by service - propagating to UI")
                        _fallDetected.value = true
                    }
                }
                delay(300)
            }
        }
    }

    fun stopSafeWalk() {
        Log.d("SafeWalkViewModel", "Stopping Safe Walk session")
        timerJob?.cancel()
        servicePollingJob?.cancel()

        try {
            safeWalkService?.stopSafeWalk()
        } catch (e: Exception) {
            Log.e("SafeWalkViewModel", "Error stopping service: ${e.message}")
            e.printStackTrace()
        }

        _isRunning.value = false
        _elapsedTime.value = 0L
        _fallDetected.value = false

        // unbind using the same context we used to bind, otherwise android throws an error
        try {
            if (isBound && boundContext != null) {
                boundContext?.unbindService(serviceConnection)
                isBound = false
                boundContext = null
                Log.d("SafeWalkViewModel", "Service unbound successfully")
            }
        } catch (e: Exception) {
            Log.e("SafeWalkViewModel", "Error unbinding service: ${e.message}")
            e.printStackTrace()
            isBound = false
            boundContext = null
        }

        safeWalkService = null
        Log.d("SafeWalkViewModel", "Safe Walk session stopped")
    }

    fun resetFallDetection() {
        Log.d("SafeWalkViewModel", "Resetting fall detection - user indicated OK")
        safeWalkService?.resetFallDetection()
        _fallDetected.value = false
    }

    fun updateFromService() {
        // Now handled by polling job, but keep for compatibility
        safeWalkService?.let { service ->
            _currentLocation.value = service.currentLocation.value
            if (service.fallDetected.value && !_fallDetected.value) {
                _fallDetected.value = true
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        servicePollingJob?.cancel()
        try {
            if (isBound && boundContext != null) {
                boundContext?.unbindService(serviceConnection)
                isBound = false
                boundContext = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
