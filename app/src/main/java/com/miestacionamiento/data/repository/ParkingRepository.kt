package com.miestacionamiento.data.repository

import androidx.lifecycle.LiveData
import com.miestacionamiento.data.local.dao.ParkingDao
import com.miestacionamiento.data.local.entity.ParkingEntity
import com.miestacionamiento.data.model.Parking
import com.miestacionamiento.data.remote.ApiService
import com.miestacionamiento.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ParkingRepository(
    private val dao: ParkingDao,
    private val api: ApiService = RetrofitClient.instance
) {

    val allParkings: LiveData<List<ParkingEntity>> = dao.getAllParkings()
    val savedParkings: LiveData<List<ParkingEntity>> = dao.getSavedParkings()
    val recentlyViewed: LiveData<List<ParkingEntity>> = dao.getRecentlyViewedParkings()

    fun searchParkings(query: String): LiveData<List<ParkingEntity>> = dao.searchParkings(query)

    suspend fun refreshFromApi(userId: Int? = null) = withContext(Dispatchers.IO) {
        try {
            val response = api.getParkings(userId?.takeIf { it > 0 })
            if (response.isSuccessful) {
                val apiParkings = response.body() ?: return@withContext
                // Preservar isSaved local para no perder favoritos del usuario
                val savedIds = dao.getSavedParkingIds().toSet()
                dao.upsertParkings(apiParkings.map { p ->
                    p.toEntity().copy(isSaved = savedIds.contains(p.id) || p.isSaved)
                })
            }
        } catch (e: Exception) {
            // Sin red: se usan los datos en caché de Room
        }
    }

    suspend fun initializeIfEmpty(userId: Int? = null) = withContext(Dispatchers.IO) {
        refreshFromApi(userId)
        if (dao.count() == 0) {
            // Fallback de emergencia si el API no responde y Room está vacío
            dao.insertParkings(mockParkings().map { it.toEntity() })
        }
    }

    suspend fun toggleSaved(id: Int, saved: Boolean) = withContext(Dispatchers.IO) {
        dao.updateSavedStatus(id, saved)
    }

    suspend fun markRecentlyViewed(id: Int) = withContext(Dispatchers.IO) {
        dao.updateRecentlyViewed(id, true)
    }

    suspend fun getParkingById(id: Int): ParkingEntity? = withContext(Dispatchers.IO) {
        dao.getParkingById(id)
    }

    private fun Parking.toEntity() = ParkingEntity(
        id, name, description, address, imageUrl,
        latitude, longitude, pricePerHour,
        availableSpots, totalSpots, rating, reviewCount, isSaved, isRecentlyViewed
    )

    companion object {
        fun mockParkings() = listOf(
            Parking(1, "Estacionamiento San Borja",
                "Estacionamiento subterráneo en el corazón de Santiago, acceso directo al metro y a la Alameda.",
                "Av. Libertador B. O'Higgins 3322, Santiago Centro",
                "https://images.unsplash.com/photo-1506521781263-d8422e82f27a?w=600",
                -33.4475, -70.6527, 1200.0, 45, 120, 4.5f, 238),
            Parking(2, "Cochera Providencia",
                "Amplio estacionamiento cubierto con servicio de valet y vigilancia permanente.",
                "Av. Providencia 1234, Providencia",
                "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=600",
                -33.4312, -70.6126, 1500.0, 12, 80, 4.2f, 156),
            Parking(3, "Parking Costanera Center",
                "Moderno estacionamiento en el mall más grande de Chile.",
                "Av. Andrés Bello 2425, Providencia",
                "https://images.unsplash.com/photo-1573348722427-f1d6819fdf98?w=600",
                -33.4177, -70.6065, 1800.0, 87, 200, 4.7f, 412)
        )
    }
}
