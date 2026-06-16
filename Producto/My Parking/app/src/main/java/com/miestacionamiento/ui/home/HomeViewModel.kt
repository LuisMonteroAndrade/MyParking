package com.miestacionamiento.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.miestacionamiento.MiEstacionamientoApp
import com.miestacionamiento.data.local.entity.ParkingEntity
import com.miestacionamiento.utils.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MiEstacionamientoApp).repository
    private val prefs = PreferencesManager(application)

    val popularParkings: LiveData<List<ParkingEntity>> = repository.allParkings
    val recentlyViewed: LiveData<List<ParkingEntity>> = repository.recentlyViewed

    init {
        viewModelScope.launch {
            val userId = prefs.userId.first()
            repository.refreshFromApi(userId.takeIf { it > 0 })
        }
    }

    fun search(query: String): LiveData<List<ParkingEntity>> = repository.searchParkings(query)
}
