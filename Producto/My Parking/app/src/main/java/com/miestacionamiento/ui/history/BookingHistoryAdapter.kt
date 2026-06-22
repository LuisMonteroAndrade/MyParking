package com.miestacionamiento.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miestacionamiento.R
import com.miestacionamiento.data.model.DriverBooking
import com.miestacionamiento.databinding.ItemBookingHistoryBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class BookingHistoryAdapter(
    private val onRebook: (DriverBooking) -> Unit
) : ListAdapter<DriverBooking, BookingHistoryAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookingHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onRebook)
    }

    class ViewHolder(private val binding: ItemBookingHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: DriverBooking, onRebook: (DriverBooking) -> Unit) {
            val ctx = binding.root.context

            binding.tvParkingName.text = booking.parkingName
            binding.tvParkingAddress.text = booking.parkingAddress ?: ""

            val fmt = NumberFormat.getNumberInstance(Locale("es", "CL"))
            binding.tvAmount.text = "$${fmt.format(booking.amount.toLong())}"

            val suffix = if (booking.hours > 1) "s" else ""
            binding.tvHours.text = "${booking.hours} hora$suffix"
            binding.tvDate.text = formatDate(booking.createdAt)

            when (booking.status) {
                "COMPLETED" -> {
                    binding.chipStatus.text = "Completada"
                    binding.chipStatus.chipBackgroundColor =
                        android.content.res.ColorStateList.valueOf(
                            ContextCompat.getColor(ctx, R.color.success)
                        )
                    binding.chipStatus.setTextColor(ContextCompat.getColor(ctx, android.R.color.white))
                    binding.btnRebook.isEnabled = true
                }
                "PENDING" -> {
                    binding.chipStatus.text = "En curso"
                    binding.chipStatus.chipBackgroundColor =
                        android.content.res.ColorStateList.valueOf(
                            ContextCompat.getColor(ctx, R.color.warning)
                        )
                    binding.chipStatus.setTextColor(ContextCompat.getColor(ctx, android.R.color.white))
                    binding.btnRebook.isEnabled = false
                }
                else -> {
                    binding.chipStatus.text = "Cancelada"
                    binding.chipStatus.chipBackgroundColor =
                        android.content.res.ColorStateList.valueOf(
                            ContextCompat.getColor(ctx, R.color.error)
                        )
                    binding.chipStatus.setTextColor(ContextCompat.getColor(ctx, android.R.color.white))
                    binding.btnRebook.isEnabled = true
                }
            }

            binding.btnRebook.setOnClickListener { onRebook(booking) }
        }

        private fun formatDate(isoDate: String?): String {
            if (isoDate == null) return ""
            return try {
                val inputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val outputFmt = SimpleDateFormat("d 'de' MMM yyyy, HH:mm", Locale("es", "CL")).apply {
                    timeZone = TimeZone.getTimeZone("America/Santiago")
                }
                val date = inputFmt.parse(isoDate) ?: return isoDate
                outputFmt.format(date)
            } catch (_: Exception) {
                isoDate.take(10)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<DriverBooking>() {
            override fun areItemsTheSame(a: DriverBooking, b: DriverBooking) = a.id == b.id
            override fun areContentsTheSame(a: DriverBooking, b: DriverBooking) = a == b
        }
    }
}
