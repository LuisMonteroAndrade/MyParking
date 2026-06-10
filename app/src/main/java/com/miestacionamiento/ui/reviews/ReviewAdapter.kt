package com.miestacionamiento.ui.reviews

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miestacionamiento.data.model.Review
import com.miestacionamiento.databinding.ItemReviewBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewAdapter(
    private val isOwner: Boolean = false,
    private val onRespond: ((Review, String) -> Unit)? = null
) : ListAdapter<Review, ReviewAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            binding.tvReviewUserName.text = review.userName
            binding.ratingBarReview.rating = review.rating.toFloat()

            val dateFormatIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val dateFormatOut = SimpleDateFormat("dd MMM yyyy", Locale("es"))
            val date = try { dateFormatIn.parse(review.createdAt) } catch (e: Exception) { null }
            binding.tvReviewDate.text = if (date != null) dateFormatOut.format(date) else ""

            if (!review.comment.isNullOrBlank()) {
                binding.tvReviewComment.text = review.comment
                binding.tvReviewComment.visibility = View.VISIBLE
            } else {
                binding.tvReviewComment.visibility = View.GONE
            }

            if (!review.ownerResponse.isNullOrBlank()) {
                binding.tvOwnerResponse.text = review.ownerResponse
                binding.layoutOwnerResponse.visibility = View.VISIBLE
            } else {
                binding.layoutOwnerResponse.visibility = View.GONE
            }

            if (isOwner && review.ownerResponse.isNullOrBlank()) {
                binding.btnRespond.visibility = View.VISIBLE
                binding.btnRespond.setOnClickListener {
                    showResponseDialog(review)
                }
            } else {
                binding.btnRespond.visibility = View.GONE
            }
        }

        private fun showResponseDialog(review: Review) {
            val input = EditText(binding.root.context).apply {
                hint = "Escribe tu respuesta..."
                setPadding(32, 16, 32, 16)
            }
            AlertDialog.Builder(binding.root.context)
                .setTitle("Responder reseña")
                .setView(input)
                .setPositiveButton("Publicar") { _, _ ->
                    val text = input.text.toString().trim()
                    if (text.isNotEmpty()) onRespond?.invoke(review, text)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Review, newItem: Review) = oldItem == newItem
    }
}
