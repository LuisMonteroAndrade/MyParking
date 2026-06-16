package com.miestacionamiento.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miestacionamiento.data.model.Message
import com.miestacionamiento.databinding.ItemMessageReceivedBinding
import com.miestacionamiento.databinding.ItemMessageSentBinding
import java.text.SimpleDateFormat
import java.util.Locale

class MessageAdapter(
    private val currentUserId: Int
) : ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_SENT = 1
        private const val TYPE_RECEIVED = 2
    }

    inner class SentViewHolder(private val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.tvMessageContent.text = message.content
            binding.tvMessageTime.text = formatTime(message.createdAt)
        }
    }

    inner class ReceivedViewHolder(private val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.tvMessageContent.text = message.content
            binding.tvMessageTime.text = formatTime(message.createdAt)
        }
    }

    private fun formatTime(createdAt: String): String {
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        )
        val out = SimpleDateFormat("HH:mm", Locale.getDefault())
        for (fmt in formats) {
            try { return out.format(fmt.parse(createdAt)!!) } catch (_: Exception) {}
        }
        return ""
    }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).senderId == currentUserId) TYPE_SENT else TYPE_RECEIVED

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == TYPE_SENT) {
            SentViewHolder(
                ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        } else {
            ReceivedViewHolder(
                ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SentViewHolder -> holder.bind(getItem(position))
            is ReceivedViewHolder -> holder.bind(getItem(position))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Message, newItem: Message) = oldItem == newItem
    }
}
