package com.miestacionamiento.data.model

data class Conversation(
    val id: Int,
    val driverId: Int,
    val ownerId: Int,
    val parkingId: Int?,
    val lastMessageAt: String?,
    val driverName: String,
    val driverPhoto: String?,
    val ownerName: String,
    val ownerPhoto: String?,
    val parkingName: String?,
    val lastMessage: String?,
    val unreadCount: Int
)

data class Message(
    val id: Int,
    val senderId: Int,
    val senderName: String,
    val content: String,
    val isRead: Boolean,
    val createdAt: String
)

data class CreateMessageRequest(
    val content: String
)

data class StartConversationRequest(
    val parkingId: Int
)

data class StartConversationResponse(
    val id: Int,
    val parkingName: String,
    val ownerId: Int,
    val driverId: Int
)
