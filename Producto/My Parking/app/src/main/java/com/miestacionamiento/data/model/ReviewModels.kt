package com.miestacionamiento.data.model

data class Review(
    val id: Int,
    val rating: Int,
    val comment: String?,
    val ownerResponse: String?,
    val ownerResponseAt: String?,
    val createdAt: String,
    val userId: Int,
    val userName: String,
    val userPhoto: String?,
    val parkingId: Int? = null,
    val parkingName: String? = null
)

data class CreateReviewRequest(
    val parkingId: Int,
    val rating: Int,
    val comment: String?
)

data class ReviewResponseRequest(
    val response: String
)
