package com.miestacionamiento.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miestacionamiento.data.local.entity.ParkingEntity
import com.miestacionamiento.databinding.ItemParkingHorizontalBinding
import com.miestacionamiento.utils.loadUrl
import com.miestacionamiento.utils.toCurrencyString
import com.miestacionamiento.utils.toStarString

class ParkingHorizontalAdapter(
    private val onItemClick: (ParkingEntity) -> Unit
) : ListAdapter<ParkingEntity, ParkingHorizontalAdapter.VH>(DiffCallback()) {

    inner class VH(private val binding: ItemParkingHorizontalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(parking: ParkingEntity) {
            binding.tvName.text = parking.name
            binding.tvAddress.text = parking.address
            binding.tvPrice.text = parking.pricePerHour.toCurrencyString()
            binding.tvRating.text = parking.rating.toStarString()
            binding.tvSpots.text = "${parking.availableSpots} disponibles"
            binding.ivParking.loadUrl(parking.imageUrl)
            binding.root.setOnClickListener { onItemClick(parking) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemParkingHorizontalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<ParkingEntity>() {
        override fun areItemsTheSame(old: ParkingEntity, new: ParkingEntity) = old.id == new.id
        override fun areContentsTheSame(old: ParkingEntity, new: ParkingEntity) = old == new
    }
}
