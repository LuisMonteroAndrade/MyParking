package com.miestacionamiento.ui.owner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miestacionamiento.R
import com.miestacionamiento.data.model.RecentBooking
import com.miestacionamiento.databinding.ItemRecentBookingBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class RecentBookingAdapter : ListAdapter<RecentBooking, RecentBookingAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentBookingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemRecentBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: RecentBooking) {
            binding.tvParkingName.text = booking.parkingName
            binding.tvDriverName.text = "${booking.driverName} • ${booking.hours} h"

            val fmt = NumberFormat.getNumberInstance(Locale("es", "CL"))
            binding.tvAmount.text = "$${fmt.format(booking.amount.toLong())}"

            val dateLabel = formatDate(booking.createdAt)
            binding.tvDate.text = dateLabel

            val ctx = binding.root.context
            when (booking.status) {
                "COMPLETED" -> {
                    binding.tvStatus.text = "Completado"
                    binding.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.success))
                }
                "PENDING" -> {
                    binding.tvStatus.text = "Pendiente"
                    binding.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.warning))
                }
                else -> {
                    binding.tvStatus.text = "Cancelado"
                    binding.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.error))
                }
            }
        }

        private fun formatDate(isoDate: String?): String {
            if (isoDate == null) return ""
            return try {
                val inputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val outputFmt = SimpleDateFormat("d MMM, HH:mm", Locale("es", "CL")).apply {
                    timeZone = TimeZone.getTimeZone("America/Santiago")
                }
                val date = inputFmt.parse(isoDate) ?: return isoDate
                outputFmt.format(date)
            } catch (e: Exception) {
                isoDate.take(10)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RecentBooking>() {
            override fun areItemsTheSame(a: RecentBooking, b: RecentBooking) = a.id == b.id
            override fun areContentsTheSame(a: RecentBooking, b: RecentBooking) = a == b
        }
    }
}
