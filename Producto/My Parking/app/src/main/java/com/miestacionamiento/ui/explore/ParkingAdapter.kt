package com.miestacionamiento.ui.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miestacionamiento.data.local.entity.ParkingEntity
import com.miestacionamiento.databinding.ItemParkingBinding
import com.miestacionamiento.utils.loadUrl
import com.miestacionamiento.utils.toCurrencyString
import com.miestacionamiento.utils.toStarString

class ParkingAdapter(
    private val onItemClick: (ParkingEntity) -> Unit,
    private val onSaveClick: (ParkingEntity) -> Unit
) : ListAdapter<ParkingEntity, ParkingAdapter.VH>(DiffCallback()) {

    inner class VH(private val binding: ItemParkingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(parking: ParkingEntity) {
            binding.tvName.text = parking.name
            binding.tvAddress.text = parking.address
            binding.tvDescription.text = parking.description
            binding.tvPrice.text = parking.pricePerHour.toCurrencyString()
            binding.tvRating.text = parking.rating.toStarString()
            binding.tvSpots.text = "${parking.availableSpots}/${parking.totalSpots} lugares"
            binding.ivParking.loadUrl(parking.imageUrl)
            binding.ivSave.setImageResource(
                if (parking.isSaved) com.miestacionamiento.R.drawable.ic_bookmark_filled
                else com.miestacionamiento.R.drawable.ic_bookmark
            )
            binding.root.setOnClickListener { onItemClick(parking) }
            binding.ivSave.setOnClickListener { onSaveClick(parking) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemParkingBinding.inflate(
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
