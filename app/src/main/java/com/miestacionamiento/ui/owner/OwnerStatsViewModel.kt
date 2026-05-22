package com.miestacionamiento.ui.owner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miestacionamiento.data.model.OwnerStats
import com.miestacionamiento.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class OwnerStatsViewModel : ViewModel() {

    private val api = RetrofitClient.instance

    private val _stats = MutableLiveData<OwnerStats?>()
    val stats: LiveData<OwnerStats?> = _stats

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getOwnerStats()
                if (response.isSuccessful) {
                    _stats.value = response.body()
                } else {
                    _error.value = "Error al obtener estadísticas (${response.code()})"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión con el servidor"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
