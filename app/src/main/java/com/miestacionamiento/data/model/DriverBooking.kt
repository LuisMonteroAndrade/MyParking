package com.miestacionamiento.data.model

data class DriverBooking(
    val id: Int,
    val parkingId: Int,
    val parkingName: String,
    val parkingAddress: String?,
    val amount: Double,
    val status: String,
    val hours: Int,
    val createdAt: String?
)
