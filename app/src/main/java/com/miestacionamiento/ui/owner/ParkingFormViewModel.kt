package com.miestacionamiento.ui.owner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miestacionamiento.data.model.CreateParkingRequest
import com.miestacionamiento.data.model.OwnerParking
import com.miestacionamiento.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class ParkingFormViewModel : ViewModel() {

    private val api = RetrofitClient.instance

    private val _parking = MutableLiveData<OwnerParking?>()
    val parking: LiveData<OwnerParking?> = _parking

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSaving = MutableLiveData(false)
    val isSaving: LiveData<Boolean> = _isSaving

    private val _saveSuccess = MutableLiveData<Boolean?>()
    val saveSuccess: LiveData<Boolean?> = _saveSuccess

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadParking(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allParkings = api.getOwnerParkings()
                if (allParkings.isSuccessful) {
                    _parking.value = allParkings.body()?.find { it.id == id }
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar el estacionamiento"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveParking(
        parkingId: Int,
        name: String,
        description: String,
        address: String,
        pricePerHour: Double,
        availableSpots: Int,
        totalSpots: Int,
        imageUrl: String
    ) {
        val request = CreateParkingRequest(
            name = name,
            description = description,
            address = address,
            pricePerHour = pricePerHour,
            availableSpots = availableSpots,
            totalSpots = totalSpots,
            imageUrl = imageUrl
        )

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val response = if (parkingId == -1) {
                    api.createParking(request)
                } else {
                    api.updateParking(parkingId, request)
                }

                if (response.isSuccessful) {
                    _saveSuccess.value = true
                } else {
                    _error.value = "Error al guardar el estacionamiento (${response.code()})"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión con el servidor"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearMessages() {
        _error.value = null
        _saveSuccess.value = null
    }
}
