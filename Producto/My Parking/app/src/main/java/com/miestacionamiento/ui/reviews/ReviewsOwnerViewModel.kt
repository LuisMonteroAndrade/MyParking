package com.miestacionamiento.ui.reviews

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.miestacionamiento.data.model.Review
import com.miestacionamiento.data.model.ReviewResponseRequest
import com.miestacionamiento.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class ReviewsOwnerViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.instance

    private val _reviews = MutableLiveData<List<Review>>(emptyList())
    val reviews: LiveData<List<Review>> = _reviews

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    fun loadReviews() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getOwnerReviews()
                if (response.isSuccessful) {
                    _reviews.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("ReviewsOwnerVM", "Error cargando reseñas", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun respondToReview(reviewId: Int, response: String) {
        viewModelScope.launch {
            try {
                val result = api.respondToReview(reviewId, ReviewResponseRequest(response))
                if (result.isSuccessful) {
                    _message.value = "Respuesta publicada"
                    loadReviews()
                } else {
                    _message.value = "No se pudo publicar la respuesta"
                }
            } catch (e: Exception) {
                Log.e("ReviewsOwnerVM", "Error respondiendo", e)
                _message.value = "Error de conexión"
            }
        }
    }

    fun clearMessage() { _message.value = null }
}
