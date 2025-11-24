package week11.stn2002.safestride.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import week11.stn2002.safestride.data.repository.AuthRepository
import week11.stn2002.safestride.util.Resource

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<Unit>?>(null)
    val loginState: StateFlow<Resource<Unit>?> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<Resource<Unit>?>(null)
    val registerState: StateFlow<Resource<Unit>?> = _registerState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<Resource<Unit>?>(null)
    val resetPasswordState: StateFlow<Resource<Unit>?> = _resetPasswordState.asStateFlow()

    val isUserLoggedIn: Boolean get() = repository.currentUser != null

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            val result = repository.login(email, password)
            _loginState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Login failed")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun register(email: String, password: String, emergencyContact: String) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            val result = repository.register(email, password, emergencyContact)
            _registerState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Registration failed")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPasswordState.value = Resource.Loading()
            val result = repository.resetPassword(email)
            _resetPasswordState.value = result
        }
    }

    fun logout() {
        repository.logout()
    }

    fun resetLoginState() {
        _loginState.value = null
    }

    fun resetRegisterState() {
        _registerState.value = null
    }

    fun resetResetPasswordState() {
        _resetPasswordState.value = null
    }
}
