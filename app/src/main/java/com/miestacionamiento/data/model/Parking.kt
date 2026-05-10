package com.miestacionamiento.data.model

data class Parking(
    val id: Int,
    val name: String,
    val description: String,
    val address: String,
    val imageUrl: String,
    val latitude: Double,
    val longitude: Double,
    val pricePerHour: Double,
    val availableSpots: Int,
    val totalSpots: Int,
    val rating: Float,
    val reviewCount: Int,
    var isSaved: Boolean = false,
    var isRecentlyViewed: Boolean = false
)
