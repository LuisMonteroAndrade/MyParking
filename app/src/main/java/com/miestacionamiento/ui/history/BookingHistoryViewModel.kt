package com.miestacionamiento.ui.history

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miestacionamiento.data.model.DriverBooking
import com.miestacionamiento.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class BookingHistoryViewModel : ViewModel() {

    private val api = RetrofitClient.instance

    private val _allBookings = MutableLiveData<List<DriverBooking>>(emptyList())

    private val _bookings = MutableLiveData<List<DriverBooking>>(emptyList())
    val bookings: LiveData<List<DriverBooking>> = _bookings

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _totalBookings = MutableLiveData(0)
    val totalBookings: LiveData<Int> = _totalBookings

    private val _totalSpent = MutableLiveData(0.0)
    val totalSpent: LiveData<Double> = _totalSpent

    private val _totalHours = MutableLiveData(0)
    val totalHours: LiveData<Int> = _totalHours

    private var activeFilter = "ALL"

    init {
        load()
    }

    fun load() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = api.getMyBookings()
                if (response.isSuccessful) {
                    val list = response.body() ?: emptyList()
                    _allBookings.value = list
                    applyFilter(activeFilter)
                    updateSummary(list)
                } else {
                    _error.value = "No se pudo cargar el historial"
                }
            } catch (e: Exception) {
                Log.e("BookingHistoryVM", "Error cargando historial", e)
                _error.value = "Error de conexión"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterAll() {
        activeFilter = "ALL"
        applyFilter("ALL")
    }

    fun filterCompleted() {
        activeFilter = "COMPLETED"
        applyFilter("COMPLETED")
    }

    fun filterPending() {
        activeFilter = "PENDING"
        applyFilter("PENDING")
    }

    fun filterFailed() {
        activeFilter = "FAILED"
        applyFilter("FAILED")
    }

    private fun applyFilter(status: String) {
        val all = _allBookings.value ?: return
        _bookings.value = if (status == "ALL") all else all.filter { it.status == status }
    }

    private fun updateSummary(list: List<DriverBooking>) {
        val completed = list.filter { it.status == "COMPLETED" }
        _totalBookings.value = list.size
        _totalSpent.value = completed.sumOf { it.amount }
        _totalHours.value = completed.sumOf { it.hours }
    }

    fun clearError() {
        _error.value = null
    }
}
