package com.miestacionamiento.ui.auth

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _state = MutableLiveData<AuthState>()
    val state: LiveData<AuthState> = _state

    fun register(name: String, email: String, password: String, confirmPassword: String, userType: String) {
        when {
            name.isBlank() || email.isBlank() || password.isBlank() ->
                _state.value = AuthState.Error("Por favor completa todos los campos")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                _state.value = AuthState.Error("Ingresa un email válido")
            password.length < 6 ->
                _state.value = AuthState.Error("La contraseña debe tener al menos 6 caracteres")
            password != confirmPassword ->
                _state.value = AuthState.Error("Las contraseñas no coinciden")
            else -> viewModelScope.launch {
                _state.value = AuthState.Loading
                delay(1500)
                _state.value = AuthState.Success
            }
        }
    }
}
