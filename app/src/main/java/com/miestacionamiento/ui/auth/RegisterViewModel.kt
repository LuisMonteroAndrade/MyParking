package com.miestacionamiento.ui.auth

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miestacionamiento.data.model.RegisterRequest
import com.miestacionamiento.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _state = MutableLiveData<AuthState>()
    val state: LiveData<AuthState> = _state

    private val api = RetrofitClient.instance

    fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        userType: String,
        vehicleBrand: String? = null,
        vehiclePlate: String? = null,
        address: String? = null,
        commune: String? = null,
        region: String? = null
    ) {
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
                try {
                    val response = api.register(
                        RegisterRequest(
                            name = name.trim(),
                            email = email.trim(),
                            password = password,
                            userType = userType,
                            vehicleBrand = vehicleBrand?.takeIf { it.isNotBlank() },
                            vehiclePlate = vehiclePlate?.takeIf { it.isNotBlank() },
                            address = address?.takeIf { it.isNotBlank() },
                            commune = commune?.takeIf { it.isNotBlank() },
                            region = region?.takeIf { it.isNotBlank() }
                        )
                    )
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        RetrofitClient.authInterceptor.setToken(body.token)
                        _state.value = AuthState.SuccessWithData(
                            token = body.token,
                            userId = body.user.id,
                            name = body.user.name,
                            email = body.user.email,
                            userType = body.user.userType,
                            vehicleBrand = body.user.vehicleBrand,
                            vehiclePlate = body.user.vehiclePlate,
                            address = body.user.address,
                            commune = body.user.commune,
                            region = body.user.region
                        )
                    } else {
                        val errorMsg = when (response.code()) {
                            409 -> "El email ya está registrado"
                            else -> "Error al registrar (${response.code()})"
                        }
                        _state.value = AuthState.Error(errorMsg)
                    }
                } catch (e: Exception) {
                    _state.value = AuthState.Error("No se pudo conectar al servidor. Verifica que el backend esté corriendo.")
                }
            }
        }
    }
}
