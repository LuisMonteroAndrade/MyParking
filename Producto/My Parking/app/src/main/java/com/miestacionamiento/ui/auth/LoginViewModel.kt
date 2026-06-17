package com.miestacionamiento.ui.auth

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.miestacionamiento.data.model.LoginRequest
import com.miestacionamiento.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

    private val _state = MutableLiveData<AuthState>()
    val state: LiveData<AuthState> = _state

    private val api = RetrofitClient.instance

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
            try {
                val response = api.login(LoginRequest(email.trim(), password))
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
                        401 -> "Email o contraseña incorrectos"
                        else -> "Error al iniciar sesión (${response.code()})"
                    }
                    _state.value = AuthState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error de conexión: ${e.javaClass.simpleName}: ${e.message}", e)
                _state.value = AuthState.Error("No se pudo conectar al servidor. Verifica que el backend esté corriendo.")
            }
        }
    }

    fun registerFcmToken() {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                api.registerFcmToken(mapOf("token" to token))
            } catch (e: Exception) {
                Log.e("LoginVM", "Error registrando FCM token", e)
            }
        }
    }

    fun loginWithGoogle() {
        _state.value = AuthState.Error("Login con Google no disponible aún")
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Success : AuthState()
    data class SuccessWithData(
        val token: String,
        val userId: Int,
        val name: String,
        val email: String,
        val userType: String,
        val vehicleBrand: String? = null,
        val vehiclePlate: String? = null,
        val address: String? = null,
        val commune: String? = null,
        val region: String? = null
    ) : AuthState()
    data class Error(val message: String) : AuthState()
}
