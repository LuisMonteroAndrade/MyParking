package com.miestacionamiento.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.miestacionamiento.R
import com.miestacionamiento.ui.main.MainActivity
import java.text.NumberFormat
import java.util.Locale

object NotificationHelper {

    // IDs v2: fuerzan recreación de canales con la importancia correcta
    const val CHANNEL_BOOKINGS = "channel_bookings_v2"
    const val CHANNEL_PAYMENTS = "channel_payments_v2"
    const val CHANNEL_CHAT    = "channel_chat_v2"
    const val CHANNEL_REVIEWS = "channel_reviews_v2"

    private val VIBRATION_PATTERN = longArrayOf(0, 250, 150, 250)

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channels = listOf(
                NotificationChannel(CHANNEL_BOOKINGS, "Reservas", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Confirmaciones y estados de reservas"
                    enableVibration(true)
                    vibrationPattern = VIBRATION_PATTERN
                    setSound(soundUri, audioAttr)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                },
                NotificationChannel(CHANNEL_PAYMENTS, "Pagos", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Confirmaciones de pago recibido"
                    enableVibration(true)
                    vibrationPattern = VIBRATION_PATTERN
                    setSound(soundUri, audioAttr)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                },
                NotificationChannel(CHANNEL_CHAT, "Mensajes", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Mensajes entre conductores y propietarios"
                    enableVibration(true)
                    vibrationPattern = VIBRATION_PATTERN
                    setSound(soundUri, audioAttr)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                },
                NotificationChannel(CHANNEL_REVIEWS, "Reseñas", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Nuevas reseñas en tus estacionamientos"
                    enableVibration(true)
                    vibrationPattern = VIBRATION_PATTERN
                    setSound(soundUri, audioAttr)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                }
            )

            // Eliminar canales viejos para evitar confusión
            listOf("channel_bookings", "channel_payments", "channel_chat", "channel_reviews")
                .forEach { manager.deleteNotificationChannel(it) }

            channels.forEach { manager.createNotificationChannel(it) }
        }
    }

    // --- Conductor ---

    fun showBookingConfirmed(context: Context, parkingName: String, hours: Int, bookingId: Int) {
        val suffix = if (hours > 1) "s" else ""
        notify(
            context, bookingId, CHANNEL_BOOKINGS,
            "¡Reserva confirmada!",
            "Tu reserva en $parkingName por $hours hora$suffix fue confirmada.",
            mainIntent(context, "BOOKING_CONFIRMED", bookingId)
        )
    }

    fun showBookingFailed(context: Context, bookingId: Int) {
        notify(
            context, bookingId + 1000, CHANNEL_BOOKINGS,
            "Pago rechazado",
            "Tu pago no pudo ser procesado. Intenta nuevamente.",
            mainIntent(context, "BOOKING_FAILED", bookingId)
        )
    }

    fun showBookingExpiringSoon(context: Context, parkingName: String, minutesLeft: Int, bookingId: Int) {
        notify(
            context, bookingId + 8000, CHANNEL_BOOKINGS,
            "Tu tiempo está por vencer",
            "Te quedan $minutesLeft minutos en $parkingName.",
            mainIntent(context, "BOOKING_EXPIRING", bookingId)
        )
    }

    fun showReviewReminder(context: Context, parkingName: String, bookingId: Int) {
        notify(
            context, bookingId + 6000, CHANNEL_REVIEWS,
            "¿Cómo fue tu experiencia?",
            "Cuéntanos qué tal estuvo $parkingName. ¡Tu opinión ayuda a otros conductores!",
            mainIntent(context, "REVIEW_REMINDER", bookingId)
        )
    }

    // --- Propietario ---

    fun showNewBooking(context: Context, driverName: String, parkingName: String, hours: Int, bookingId: Int) {
        val suffix = if (hours > 1) "s" else ""
        notify(
            context, bookingId + 2000, CHANNEL_BOOKINGS,
            "Nueva reserva",
            "$driverName reservó $parkingName por $hours hora$suffix.",
            mainIntent(context, "NEW_BOOKING", bookingId)
        )
    }

    fun showPaymentReceived(context: Context, amount: Double, driverName: String, bookingId: Int) {
        val fmt = NumberFormat.getNumberInstance(Locale("es", "CL"))
        notify(
            context, bookingId + 3000, CHANNEL_PAYMENTS,
            "Pago recibido",
            "Recibiste $${fmt.format(amount.toLong())} de $driverName.",
            mainIntent(context, "PAYMENT_RECEIVED", bookingId)
        )
    }

    fun showNewReview(context: Context, parkingName: String, rating: Int, reviewId: Int) {
        val stars = "★".repeat(rating) + "☆".repeat(5 - rating)
        notify(
            context, reviewId + 5000, CHANNEL_REVIEWS,
            "Nueva reseña en $parkingName",
            "$stars — Alguien dejó una opinión en tu estacionamiento.",
            mainIntent(context, "NEW_REVIEW", reviewId)
        )
    }

    fun showParkingFull(context: Context, parkingName: String, parkingId: Int) {
        notify(
            context, parkingId + 7000, CHANNEL_BOOKINGS,
            "$parkingName está lleno",
            "No quedan cupos disponibles. Considera ajustar la disponibilidad.",
            mainIntent(context, "PARKING_FULL", parkingId)
        )
    }

    // --- Ambos ---

    fun showNewMessage(context: Context, senderName: String, preview: String, conversationId: Int) {
        notify(
            context, conversationId + 4000, CHANNEL_CHAT,
            "Mensaje de $senderName",
            preview,
            mainIntent(context, "NEW_MESSAGE", conversationId)
        )
    }

    // --- Internos ---

    private fun mainIntent(context: Context, type: String, extraId: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_type", type)
            putExtra("notification_extra_id", extraId)
        }
        return PendingIntent.getActivity(
            context, extraId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun notify(
        context: Context,
        id: Int,
        channelId: String,
        title: String,
        body: String,
        pendingIntent: PendingIntent
    ) {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            // Banner emergente y pantalla de bloqueo
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // Sonido y vibración
            .setSound(soundUri)
            .setVibrate(VIBRATION_PATTERN)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            // Comportamiento
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {
            // Permiso POST_NOTIFICATIONS no concedido
        }
    }
}
