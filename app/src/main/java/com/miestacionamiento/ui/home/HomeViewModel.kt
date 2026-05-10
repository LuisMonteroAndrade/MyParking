package com.miestacionamiento.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.miestacionamiento.MiEstacionamientoApp
import com.miestacionamiento.data.local.entity.ParkingEntity
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MiEstacionamientoApp).repository

    val popularParkings: LiveData<List<ParkingEntity>> = repository.allParkings
    val recentlyViewed: LiveData<List<ParkingEntity>> = repository.recentlyViewed

    private val _searchResults = MutableLiveData<List<ParkingEntity>>()
    val searchResults: LiveData<List<ParkingEntity>> = _searchResults

    init {
        viewModelScope.launch { repository.initializeIfEmpty() }
    }

    fun search(query: String): LiveData<List<ParkingEntity>> = repository.searchParkings(query)
}
