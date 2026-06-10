package com.miestacionamiento.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miestacionamiento.data.model.Conversation
import com.miestacionamiento.databinding.ItemConversationBinding

class ConversationAdapter(
    private val currentUserId: Int,
    private val onClick: (Conversation) -> Unit
) : ListAdapter<Conversation, ConversationAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemConversationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(conv: Conversation) {
            val contactName = if (currentUserId == conv.driverId) conv.ownerName else conv.driverName
            binding.tvContactName.text = contactName
            binding.tvParkingName.text = conv.parkingName ?: "Estacionamiento"
            binding.tvLastMessage.text = conv.lastMessage ?: "Sin mensajes aún"

            if (conv.unreadCount > 0) {
                binding.tvUnread.text = conv.unreadCount.toString()
                binding.tvUnread.visibility = View.VISIBLE
            } else {
                binding.tvUnread.visibility = View.GONE
            }

            binding.root.setOnClickListener { onClick(conv) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Conversation>() {
        override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation) =
            oldItem == newItem
    }
}
