package com.miestacionamiento.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.miestacionamiento.data.remote.RetrofitClient
import com.miestacionamiento.utils.NotificationHelper
import com.miestacionamiento.utils.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = PreferencesManager(applicationContext)
                val authToken = prefs.authToken.first()
                if (authToken.isNotBlank()) {
                    RetrofitClient.authInterceptor.setToken(authToken)
                    RetrofitClient.instance.registerFcmToken(mapOf("token" to token))
                }
            } catch (e: Exception) {
                Log.e("FCM", "Error registrando token FCM", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val type = data["type"] ?: return
        val ctx = applicationContext

        when (type) {
            "BOOKING_CONFIRMED" -> NotificationHelper.showBookingConfirmed(
                ctx,
                data["parkingName"] ?: "el estacionamiento",
                data["hours"]?.toIntOrNull() ?: 1,
                data["bookingId"]?.toIntOrNull() ?: 0
            )
            "BOOKING_FAILED" -> NotificationHelper.showBookingFailed(
                ctx,
                data["bookingId"]?.toIntOrNull() ?: 0
            )
            "BOOKING_EXPIRING" -> NotificationHelper.showBookingExpiringSoon(
                ctx,
                data["parkingName"] ?: "el estacionamiento",
                data["minutesLeft"]?.toIntOrNull() ?: 15,
                data["bookingId"]?.toIntOrNull() ?: 0
            )
            "REVIEW_REMINDER" -> NotificationHelper.showReviewReminder(
                ctx,
                data["parkingName"] ?: "el estacionamiento",
                data["bookingId"]?.toIntOrNull() ?: 0
            )
            "NEW_BOOKING" -> NotificationHelper.showNewBooking(
                ctx,
                data["driverName"] ?: "Un conductor",
                data["parkingName"] ?: "tu estacionamiento",
                data["hours"]?.toIntOrNull() ?: 1,
                data["bookingId"]?.toIntOrNull() ?: 0
            )
            "PAYMENT_RECEIVED" -> NotificationHelper.showPaymentReceived(
                ctx,
                data["amount"]?.toDoubleOrNull() ?: 0.0,
                data["driverName"] ?: "Un conductor",
                data["bookingId"]?.toIntOrNull() ?: 0
            )
            "NEW_REVIEW" -> NotificationHelper.showNewReview(
                ctx,
                data["parkingName"] ?: "tu estacionamiento",
                data["rating"]?.toIntOrNull() ?: 5,
                data["reviewId"]?.toIntOrNull() ?: 0
            )
            "PARKING_FULL" -> NotificationHelper.showParkingFull(
                ctx,
                data["parkingName"] ?: "Tu estacionamiento",
                data["parkingId"]?.toIntOrNull() ?: 0
            )
            "NEW_MESSAGE" -> NotificationHelper.showNewMessage(
                ctx,
                data["senderName"] ?: "Alguien",
                data["preview"] ?: "Tienes un nuevo mensaje",
                data["conversationId"]?.toIntOrNull() ?: 0
            )
        }
    }
}
