package com.miestacionamiento.ui.owner

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miestacionamiento.R
import com.miestacionamiento.data.model.OwnerParking
import com.miestacionamiento.databinding.ItemOwnerParkingBinding
import com.miestacionamiento.utils.loadUrl

class OwnerParkingAdapter(
    private val onEdit: (OwnerParking) -> Unit,
    private val onDelete: (OwnerParking) -> Unit,
    private val onToggleStatus: (OwnerParking) -> Unit,
    private val onViewReviews: (() -> Unit)? = null
) : ListAdapter<OwnerParking, OwnerParkingAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOwnerParkingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemOwnerParkingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(parking: OwnerParking) {
            binding.tvParkingName.text = parking.name
            binding.tvParkingAddress.text = parking.address
            binding.tvPrice.text = "$ ${parking.pricePerHour.toInt()} / hora"
            binding.tvSpots.text = "${parking.availableSpots} / ${parking.totalSpots} espacios"

            val ctx = itemView.context
            if (parking.isActive) {
                binding.chipStatus.text = ctx.getString(R.string.status_active)
                binding.chipStatus.chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(ctx, R.color.primary_light)
                )
            } else {
                binding.chipStatus.text = ctx.getString(R.string.status_frozen)
                binding.chipStatus.chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(ctx, R.color.surface_variant)
                )
            }

            if (parking.imageUrl.isNotEmpty()) {
                binding.ivParkingImage.loadUrl(parking.imageUrl)
            } else {
                binding.ivParkingImage.setImageResource(R.drawable.ic_business)
                binding.ivParkingImage.setBackgroundColor(
                    ContextCompat.getColor(ctx, R.color.surface_variant)
                )
            }

            binding.btnMore.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menuInflater.inflate(R.menu.menu_owner_parking_item, popup.menu)
                popup.menu.findItem(R.id.action_toggle_status).title =
                    if (parking.isActive) ctx.getString(R.string.freeze)
                    else ctx.getString(R.string.activate)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_edit -> { onEdit(parking); true }
                        R.id.action_delete -> { onDelete(parking); true }
                        R.id.action_toggle_status -> { onToggleStatus(parking); true }
                        R.id.action_reviews -> { onViewReviews?.invoke(); true }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<OwnerParking>() {
        override fun areItemsTheSame(oldItem: OwnerParking, newItem: OwnerParking) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: OwnerParking, newItem: OwnerParking) =
            oldItem == newItem
    }
}
