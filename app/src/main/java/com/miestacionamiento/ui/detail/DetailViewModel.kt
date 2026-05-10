package com.miestacionamiento.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.miestacionamiento.MiEstacionamientoApp
import com.miestacionamiento.data.local.entity.ParkingEntity
import kotlinx.coroutines.launch

class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MiEstacionamientoApp).repository

    private val _parking = MutableLiveData<ParkingEntity?>()
    val parking: LiveData<ParkingEntity?> = _parking

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
}
