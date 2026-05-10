package com.miestacionamiento.data.repository

import androidx.lifecycle.LiveData
import com.miestacionamiento.data.local.dao.ParkingDao
import com.miestacionamiento.data.local.entity.ParkingEntity
import com.miestacionamiento.data.model.Parking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ParkingRepository(private val dao: ParkingDao) {

    val allParkings: LiveData<List<ParkingEntity>> = dao.getAllParkings()
    val savedParkings: LiveData<List<ParkingEntity>> = dao.getSavedParkings()
    val recentlyViewed: LiveData<List<ParkingEntity>> = dao.getRecentlyViewedParkings()

    fun searchParkings(query: String): LiveData<List<ParkingEntity>> = dao.searchParkings(query)

    suspend fun initializeIfEmpty() = withContext(Dispatchers.IO) {
        if (dao.count() == 0) {
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
            Parking(1, "Parking Central Plaza",
                "Estacionamiento en el corazón de la ciudad con seguridad 24/7 y múltiples servicios para tu comodidad.",
                "Av. Corrientes 1234, Buenos Aires",
                "https://images.unsplash.com/photo-1506521781263-d8422e82f27a?w=600",
                -34.6037, -58.3816, 350.0, 45, 120, 4.5f, 238),
            Parking(2, "Garaje San Martín",
                "Amplio estacionamiento cubierto con servicio de valet parking y vigilancia permanente.",
                "Calle San Martín 567, Buenos Aires",
                "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=600",
                -34.6118, -58.3960, 280.0, 12, 80, 4.2f, 156),
            Parking(3, "Parking Puerto Madero",
                "Moderno estacionamiento frente al río con fácil acceso a Puerto Madero y restaurantes.",
                "Dique 4, Puerto Madero, Buenos Aires",
                "https://images.unsplash.com/photo-1573348722427-f1d6819fdf98?w=600",
                -34.6152, -58.3632, 450.0, 87, 200, 4.7f, 412),
            Parking(4, "Cochera Palermo",
                "Estacionamiento seguro en el barrio de Palermo con vigilancia y cámaras de seguridad.",
                "Av. Santa Fe 3600, Palermo, Buenos Aires",
                "https://images.unsplash.com/photo-1545127398-14699f92334b?w=600",
                -34.5913, -58.4130, 320.0, 5, 60, 4.0f, 89),
            Parking(5, "Parking Recoleta Premium",
                "Estacionamiento premium en el exclusivo barrio de Recoleta, cerca de museos y galerías.",
                "Av. Alvear 1800, Recoleta, Buenos Aires",
                "https://images.unsplash.com/photo-1611293388250-580b08c4a145?w=600",
                -34.5877, -58.3927, 500.0, 30, 100, 4.8f, 325),
            Parking(6, "Garaje Belgrano",
                "Cochera familiar con múltiples servicios adicionales, ideal para estadías largas.",
                "Cabildo 2200, Belgrano, Buenos Aires",
                "https://images.unsplash.com/photo-1592838064575-70ed626d3a0e?w=600",
                -34.5598, -58.4572, 250.0, 22, 70, 4.3f, 178),
            Parking(7, "Estacionamiento Microcentro",
                "Ubicado en el microcentro porteño, perfecto para visitas de negocios y trámites.",
                "Florida 800, Microcentro, Buenos Aires",
                "https://images.unsplash.com/photo-1470224114660-3f6686c562eb?w=600",
                -34.6043, -58.3741, 400.0, 18, 90, 4.1f, 203),
            Parking(8, "Cochera San Telmo",
                "Estacionamiento en el histórico barrio de San Telmo, cerca de la feria y museos.",
                "Defensa 1100, San Telmo, Buenos Aires",
                "https://images.unsplash.com/photo-1504307651254-35680f356dfd?w=600",
                -34.6218, -58.3700, 300.0, 33, 75, 4.4f, 142)
        )
    }
}
