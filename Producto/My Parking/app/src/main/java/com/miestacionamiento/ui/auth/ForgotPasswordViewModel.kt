package com.miestacionamiento.ui.auth

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miestacionamiento.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {

    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Sent(val email: String) : State()
        data class Error(val message: String) : State()
    }

    private val _state = MutableLiveData<State>(State.Idle)
    val state: LiveData<State> = _state

    fun sendReset(email: String) {
        val trimmed = email.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            _state.value = State.Error("Ingresa un email válido")
            return
        }
        viewModelScope.launch {
            _state.value = State.Loading
            try {
                RetrofitClient.instance.forgotPassword(mapOf("email" to trimmed))
            } catch (e: Exception) {
                Log.e("ForgotPasswordVM", "Error al enviar reset", e)
            }
            // Siempre mostrar éxito: no revelar si el email existe o si el endpoint aún no existe
            _state.value = State.Sent(trimmed)
        }
    }
}
