package com.miestacionamiento.ui.saved

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.miestacionamiento.MiEstacionamientoApp
import com.miestacionamiento.data.local.entity.ParkingEntity
import kotlinx.coroutines.launch

class SavedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MiEstacionamientoApp).repository
    val savedParkings: LiveData<List<ParkingEntity>> = repository.savedParkings

    fun removeFromSaved(parking: ParkingEntity) {
        viewModelScope.launch { repository.toggleSaved(parking.id, false) }
    }
}
