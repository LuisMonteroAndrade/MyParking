package com.miestacionamiento.ui.owner

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.miestacionamiento.data.model.OwnerParking
import com.miestacionamiento.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ParkingFormViewModel(application: Application) : AndroidViewModel(application) {

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
        fullAddress: String,
        description: String,
        pricePerHour: Double,
        availableSpots: Int,
        totalSpots: Int,
        latitude: Double,
        longitude: Double,
        imageUri: Uri?,
        existingImageUrl: String
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                fun String.toBody() = toRequestBody("text/plain".toMediaType())

                val imagePart = imageUri?.let { buildImagePart(it) }

                val response = if (parkingId == -1) {
                    if (imagePart == null) {
                        _error.value = "Selecciona una imagen para continuar"
                        return@launch
                    }
                    api.createParking(
                        name = name.toBody(),
                        description = description.toBody(),
                        address = fullAddress.toBody(),
                        pricePerHour = pricePerHour.toString().toBody(),
                        availableSpots = availableSpots.toString().toBody(),
                        totalSpots = totalSpots.toString().toBody(),
                        latitude = latitude.toString().toBody(),
                        longitude = longitude.toString().toBody(),
                        image = imagePart
                    )
                } else {
                    api.updateParking(
                        id = parkingId,
                        name = name.toBody(),
                        description = description.toBody(),
                        address = fullAddress.toBody(),
                        pricePerHour = pricePerHour.toString().toBody(),
                        availableSpots = availableSpots.toString().toBody(),
                        totalSpots = totalSpots.toString().toBody(),
                        latitude = latitude.toString().toBody(),
                        longitude = longitude.toString().toBody(),
                        existingImageUrl = existingImageUrl.toBody(),
                        image = imagePart
                    )
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

    private suspend fun buildImagePart(uri: Uri): MultipartBody.Part? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = getApplication<Application>().contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val ext = if (mimeType.contains("png", ignoreCase = true)) ".png" else ".jpg"
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext null
            val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", "parking$ext", requestFile)
        } catch (e: Exception) {
            null
        }
    }

    fun clearMessages() {
        _error.value = null
        _saveSuccess.value = null
    }
}
