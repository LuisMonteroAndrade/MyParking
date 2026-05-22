package com.miestacionamiento.ui.owner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.miestacionamiento.data.model.OwnerDashboardData
import com.miestacionamiento.data.remote.RetrofitClient
import com.miestacionamiento.utils.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class OwnerDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.instance
    private val prefs = PreferencesManager(application)

    private val _dashboardData = MutableLiveData<OwnerDashboardData?>()
    val dashboardData: LiveData<OwnerDashboardData?> = _dashboardData

    private val _ownerName = MutableLiveData<String>()
    val ownerName: LiveData<String> = _ownerName

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadDashboard() {
        viewModelScope.launch {
            _ownerName.value = prefs.userName.first()
            _isLoading.value = true
            try {
                val response = api.getOwnerDashboard()
                if (response.isSuccessful) {
                    _dashboardData.value = response.body()
                } else {
                    _dashboardData.value = OwnerDashboardData(0, 0, 0.0, 0, emptyList())
                }
            } catch (e: Exception) {
                _dashboardData.value = OwnerDashboardData(0, 0, 0.0, 0, emptyList())
                _error.value = "Error de conexión"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
