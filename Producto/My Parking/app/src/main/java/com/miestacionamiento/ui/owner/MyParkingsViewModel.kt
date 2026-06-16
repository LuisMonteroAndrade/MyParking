package com.miestacionamiento.ui.owner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miestacionamiento.data.model.OwnerParking
import com.miestacionamiento.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class MyParkingsViewModel : ViewModel() {

    private val api = RetrofitClient.instance

    private val _parkings = MutableLiveData<List<OwnerParking>>()
    val parkings: LiveData<List<OwnerParking>> = _parkings

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _operationResult = MutableLiveData<OperationResult?>()
    val operationResult: LiveData<OperationResult?> = _operationResult

    sealed class OperationResult {
        object Deleted : OperationResult()
        data class StatusChanged(val isActive: Boolean) : OperationResult()
    }

    fun loadParkings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getOwnerParkings()
                if (response.isSuccessful) {
                    _parkings.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Error al cargar los estacionamientos (${response.code()})"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión con el servidor"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteParking(id: Int) {
        viewModelScope.launch {
            try {
                val response = api.deleteParking(id)
                if (response.isSuccessful) {
                    _operationResult.value = OperationResult.Deleted
                    loadParkings()
                } else {
                    _error.value = "Error al eliminar el estacionamiento"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión con el servidor"
            }
        }
    }

    fun toggleParkingStatus(id: Int) {
        viewModelScope.launch {
            try {
                val response = api.toggleParkingStatus(id)
                if (response.isSuccessful) {
                    val updated = response.body()
                    _operationResult.value = OperationResult.StatusChanged(updated?.isActive ?: true)
                    loadParkings()
                } else {
                    _error.value = "Error al cambiar el estado del estacionamiento"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión con el servidor"
            }
        }
    }

    fun clearMessages() {
        _error.value = null
        _operationResult.value = null
    }
}
