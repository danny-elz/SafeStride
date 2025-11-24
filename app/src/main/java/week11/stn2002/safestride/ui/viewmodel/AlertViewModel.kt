package week11.stn2002.safestride.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import week11.stn2002.safestride.data.model.SOSAlert
import week11.stn2002.safestride.data.repository.AlertRepository
import week11.stn2002.safestride.util.Resource

class AlertViewModel(
    private val repository: AlertRepository = AlertRepository()
) : ViewModel() {

    private val _alerts = MutableStateFlow<Resource<List<SOSAlert>>>(Resource.Loading())
    val alerts: StateFlow<Resource<List<SOSAlert>>> = _alerts.asStateFlow()

    private val _createAlertState = MutableStateFlow<Resource<Unit>?>(null)
    val createAlertState: StateFlow<Resource<Unit>?> = _createAlertState.asStateFlow()

    init {
        loadAlerts()
    }

    fun loadAlerts() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _alerts.value = Resource.Success(emptyList())
            return
        }
        viewModelScope.launch {
            try {
                repository.getUserAlertsFlow(userId).collect { result ->
                    _alerts.value = result
                }
            } catch (e: Exception) {
                _alerts.value = Resource.Error(e.message ?: "Failed to load alerts")
            }
        }
    }

    fun createManualAlert(latitude: Double, longitude: Double, address: String = "") {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            _createAlertState.value = Resource.Loading()
            val alert = SOSAlert(
                userId = userId,
                latitude = latitude,
                longitude = longitude,
                isAutomatic = false,
                address = address
            )
            val result = repository.createAlert(alert)
            _createAlertState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Failed to create alert")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun createAutomaticAlert(latitude: Double, longitude: Double, address: String = "") {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            val alert = SOSAlert(
                userId = userId,
                latitude = latitude,
                longitude = longitude,
                isAutomatic = true,
                address = address
            )
            repository.createAlert(alert)
        }
    }

    fun deleteAlert(alertId: String) {
        viewModelScope.launch {
            repository.deleteAlert(alertId)
        }
    }

    fun resolveAlert(alertId: String) {
        viewModelScope.launch {
            repository.resolveAlert(alertId)
        }
    }

    fun resetCreateAlertState() {
        _createAlertState.value = null
    }
}
