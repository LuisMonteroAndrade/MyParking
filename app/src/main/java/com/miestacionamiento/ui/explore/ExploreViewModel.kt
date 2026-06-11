package com.miestacionamiento.ui.explore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.miestacionamiento.MiEstacionamientoApp
import com.miestacionamiento.data.local.entity.ParkingEntity
import com.miestacionamiento.utils.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ExploreViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MiEstacionamientoApp).repository
    private val prefs = PreferencesManager(application)

    private val _query = MutableLiveData("")
    val parkings: LiveData<List<ParkingEntity>> = _query.switchMap { q ->
        if (q.isBlank()) repository.allParkings else repository.searchParkings(q)
    }

    fun setQuery(query: String) { _query.value = query }

    fun refresh() {
        viewModelScope.launch {
            val userId = prefs.userId.first()
            repository.refreshFromApi(userId.takeIf { it > 0 })
        }
    }
}
