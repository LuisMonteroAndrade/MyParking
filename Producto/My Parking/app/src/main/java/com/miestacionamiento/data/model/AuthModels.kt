package com.miestacionamiento.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val userType: String,
    val vehicleBrand: String? = null,
    val vehiclePlate: String? = null,
    val photoUrl: String? = null,
    val address: String? = null,
    val commune: String? = null,
    val region: String? = null
)

data class AuthResponse(
    val token: String,
    val user: UserProfile
)

data class UserProfile(
    val id: Int,
    val name: String,
    val email: String,
    val userType: String,
    val vehicleBrand: String?,
    val vehiclePlate: String?,
    val photoUrl: String?,
    val address: String?,
    val commune: String?,
    val region: String?
)

data class ChangeRoleRequest(
    val userType: String,
    val address: String? = null,
    val commune: String? = null,
    val region: String? = null
)

data class SaveResponse(
    val isSaved: Boolean
)
