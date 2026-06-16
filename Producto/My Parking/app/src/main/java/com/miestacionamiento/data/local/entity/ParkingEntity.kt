package com.miestacionamiento.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parkings")
data class ParkingEntity(
    @PrimaryKey val id: Int,
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
    val isSaved: Boolean = false,
    val isRecentlyViewed: Boolean = false
)
