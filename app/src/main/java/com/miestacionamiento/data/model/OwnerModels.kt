package com.miestacionamiento.data.model

data class OwnerDashboardData(
    val activeParkings: Int,
    val totalParkings: Int,
    val monthRevenue: Double,
    val monthBookings: Int,
    val recentBookings: List<RecentBooking>
)

data class RecentBooking(
    val id: Int,
    val parkingName: String,
    val driverName: String,
    val amount: Double,
    val status: String,
    val createdAt: String?,
    val hours: Int
)

data class CreateBookingRequest(
    val parkingId: Int,
    val hours: Int,
    val paymentToken: String,
    val paymentMethod: String = "GOOGLE_PAY"
)

data class BookingResponse(
    val id: Int,
    val parkingId: Int,
    val driverId: Int,
    val amount: Double,
    val status: String,
    val hours: Int,
    val createdAt: String?
)

data class OwnerParking(
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
    val isActive: Boolean,
    val ownerId: Int?,
    val createdAt: String?
)

data class CreateParkingRequest(
    val name: String,
    val description: String,
    val address: String,
    val pricePerHour: Double,
    val availableSpots: Int,
    val totalSpots: Int,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String = ""
)

data class OwnerStats(
    val totalRevenue: Double,
    val totalBookings: Int,
    val uniqueDrivers: Int,
    val monthlyRevenue: List<Float>,
    val monthlyBookings: List<Int>,
    val monthLabels: List<String>
)

data class DeleteResponse(
    val success: Boolean,
    val message: String
)
