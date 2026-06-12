package com.miestacionamiento.ui.detail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.miestacionamiento.MiEstacionamientoApp
import com.miestacionamiento.data.local.entity.ParkingEntity
import com.miestacionamiento.data.model.BookingResponse
import com.miestacionamiento.data.model.CreateReviewRequest
import com.miestacionamiento.data.model.FlowPaymentRequest
import com.miestacionamiento.data.model.Review
import com.miestacionamiento.data.model.StartConversationRequest
import com.miestacionamiento.data.remote.RetrofitClient
import com.miestacionamiento.utils.PreferencesManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MiEstacionamientoApp).repository
    private val api = RetrofitClient.instance
    private val prefs = PreferencesManager(application)

    private val _parking = MutableLiveData<ParkingEntity?>()
    val parking: LiveData<ParkingEntity?> = _parking

    private val _bookingResult = MutableLiveData<BookingResult?>()
    val bookingResult: LiveData<BookingResult?> = _bookingResult

    private val _isBookingLoading = MutableLiveData(false)
    val isBookingLoading: LiveData<Boolean> = _isBookingLoading

    private val _reviews = MutableLiveData<List<Review>>(emptyList())
    val reviews: LiveData<List<Review>> = _reviews

    private val _reviewResult = MutableLiveData<String?>()
    val reviewResult: LiveData<String?> = _reviewResult

    private val _userType = MutableLiveData("DRIVER")
    val userType: LiveData<String> = _userType

    private val _userId = MutableLiveData(0)
    val userId: LiveData<Int> = _userId

    private val _chatConversationId = MutableLiveData<Int?>()
    val chatConversationId: LiveData<Int?> = _chatConversationId

    private val _flowPaymentUrl = MutableLiveData<String?>()
    val flowPaymentUrl: LiveData<String?> = _flowPaymentUrl

    private var pollingJob: Job? = null

    init {
        viewModelScope.launch {
            _userType.value = prefs.userType.first()
            _userId.value = prefs.userId.first()
        }
    }

    fun loadParking(id: Int) {
        viewModelScope.launch {
            _parking.value = repository.getParkingById(id)
            repository.markRecentlyViewed(id)
            loadReviews(id)
        }
    }

    fun loadReviews(parkingId: Int) {
        viewModelScope.launch {
            try {
                val response = api.getReviews(parkingId)
                if (response.isSuccessful) {
                    _reviews.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("DetailVM", "Error cargando reseñas", e)
            }
        }
    }

    fun submitReview(parkingId: Int, rating: Int, comment: String?) {
        viewModelScope.launch {
            try {
                val response = api.createReview(CreateReviewRequest(parkingId, rating, comment))
                if (response.isSuccessful) {
                    _reviewResult.value = "¡Reseña publicada!"
                    loadReviews(parkingId)
                } else {
                    _reviewResult.value = "No se pudo publicar la reseña"
                }
            } catch (e: Exception) {
                Log.e("DetailVM", "Error publicando reseña", e)
                _reviewResult.value = "Error de conexión"
            }
        }
    }

    fun startChat(parkingId: Int) {
        viewModelScope.launch {
            try {
                val response = api.startConversation(StartConversationRequest(parkingId))
                if (response.isSuccessful) {
                    _chatConversationId.value = response.body()?.id
                }
            } catch (e: Exception) {
                Log.e("DetailVM", "Error iniciando chat", e)
            }
        }
    }

    fun clearReviewResult() { _reviewResult.value = null }
    fun clearChatConversationId() { _chatConversationId.value = null }

    fun toggleSaved() {
        val current = _parking.value ?: return
        viewModelScope.launch {
            repository.toggleSaved(current.id, !current.isSaved)
            _parking.value = repository.getParkingById(current.id)
        }
    }

    fun createFlowPayment(parkingId: Int, hours: Int) {
        _isBookingLoading.value = true
        viewModelScope.launch {
            try {
                val response = api.createFlowPayment(FlowPaymentRequest(parkingId, hours))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    _flowPaymentUrl.value = body.paymentUrl
                    startStatusPolling(body.bookingId)
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Datos de reserva inválidos"
                        404 -> "Estacionamiento no disponible"
                        503 -> "Sistema de pago no configurado"
                        else -> "No se pudo crear el pago"
                    }
                    _bookingResult.value = BookingResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("DetailVM", "Error creando pago Flow", e)
                _bookingResult.value = BookingResult.Error("Error de conexión")
            } finally {
                _isBookingLoading.value = false
            }
        }
    }

    private fun startStatusPolling(bookingId: Int) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            var attempts = 0
            while (attempts < 100) {
                delay(3000)
                attempts++
                try {
                    val response = api.getBookingStatus(bookingId)
                    if (response.isSuccessful) {
                        val booking = response.body() ?: continue
                        when (booking.status) {
                            "COMPLETED" -> {
                                _bookingResult.value = BookingResult.Success(booking)
                                return@launch
                            }
                            "FAILED" -> {
                                _bookingResult.value = BookingResult.Error("El pago fue rechazado")
                                return@launch
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DetailVM", "Error en polling de pago", e)
                }
            }
            _bookingResult.value = BookingResult.Error("Tiempo de espera agotado. Si ya pagaste, tu reserva se confirmará en breve.")
        }
    }

    fun stopPaymentPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun clearFlowPaymentUrl() {
        _flowPaymentUrl.value = null
    }

    fun clearBookingResult() {
        _bookingResult.value = null
        stopPaymentPolling()
    }

    sealed class BookingResult {
        data class Success(val booking: BookingResponse) : BookingResult()
        data class Error(val message: String) : BookingResult()
    }
}
