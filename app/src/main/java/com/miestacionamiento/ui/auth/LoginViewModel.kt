package com.miestacionamiento.ui.auth

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _state = MutableLiveData<AuthState>()
    val state: LiveData<AuthState> = _state

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthState.Error("Por favor completa todos los campos")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.value = AuthState.Error("Ingresa un email válido")
            return
        }
        if (password.length < 6) {
            _state.value = AuthState.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }
        viewModelScope.launch {
            _state.value = AuthState.Loading
            delay(1200)
            _state.value = AuthState.Success
        }
    }

    fun loginWithGoogle() {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            delay(800)
            _state.value = AuthState.Success
        }
    }

    fun loginWithFacebook() {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            delay(800)
            _state.value = AuthState.Success
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
