package com.miestacionamiento.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.miestacionamiento.MiEstacionamientoApp
import com.miestacionamiento.data.local.entity.ParkingEntity
import com.miestacionamiento.data.model.BookingResponse
import com.miestacionamiento.data.model.CreateBookingRequest
import com.miestacionamiento.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MiEstacionamientoApp).repository
    private val api = RetrofitClient.instance

    private val _parking = MutableLiveData<ParkingEntity?>()
    val parking: LiveData<ParkingEntity?> = _parking

    private val _bookingResult = MutableLiveData<BookingResult?>()
    val bookingResult: LiveData<BookingResult?> = _bookingResult

    private val _isBookingLoading = MutableLiveData(false)
    val isBookingLoading: LiveData<Boolean> = _isBookingLoading

    fun loadParking(id: Int) {
        viewModelScope.launch {
            _parking.value = repository.getParkingById(id)
            repository.markRecentlyViewed(id)
        }
    }

    fun toggleSaved() {
        val current = _parking.value ?: return
        viewModelScope.launch {
            repository.toggleSaved(current.id, !current.isSaved)
            _parking.value = repository.getParkingById(current.id)
        }
    }

    fun createBooking(parkingId: Int, hours: Int, paymentToken: String, isGooglePay: Boolean) {
        _isBookingLoading.value = true
        viewModelScope.launch {
            try {
                val request = CreateBookingRequest(
                    parkingId = parkingId,
                    hours = hours,
                    paymentToken = paymentToken,
                    paymentMethod = if (isGooglePay) "GOOGLE_PAY" else "PENDING"
                )
                val response = api.createBooking(request)
                if (response.isSuccessful) {
                    _bookingResult.value = BookingResult.Success(response.body()!!)
                } else {
                    _bookingResult.value = BookingResult.Error("No se pudo confirmar la reserva")
                }
            } catch (e: Exception) {
                _bookingResult.value = BookingResult.Error("Error de conexión")
            } finally {
                _isBookingLoading.value = false
            }
        }
    }

    fun clearBookingResult() {
        _bookingResult.value = null
    }

    sealed class BookingResult {
        data class Success(val booking: BookingResponse) : BookingResult()
        data class Error(val message: String) : BookingResult()
    }
}
