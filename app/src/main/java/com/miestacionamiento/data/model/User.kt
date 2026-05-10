package com.miestacionamiento.data.model

data class User(
    val id: String = "1",
    val name: String = "Usuario Demo",
    val email: String = "demo@miestacionamiento.com",
    val photoUrl: String? = null,
    val userType: UserType = UserType.DRIVER
)

enum class UserType { DRIVER, OWNER }
