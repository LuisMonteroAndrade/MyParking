package com.miestacionamiento.ui.reviews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.miestacionamiento.databinding.FragmentReviewsOwnerBinding
import com.miestacionamiento.utils.gone
import com.miestacionamiento.utils.visible

class ReviewsOwnerFragment : Fragment() {

    private var _binding: FragmentReviewsOwnerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReviewsOwnerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewsOwnerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val adapter = ReviewAdapter(isOwner = true) { review, responseText ->
            viewModel.respondToReview(review.id, responseText)
        }
        binding.rvOwnerReviews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOwnerReviews.adapter = adapter

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) binding.progressBar.visible() else binding.progressBar.gone()
        }

        viewModel.reviews.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            if (list.isEmpty()) {
                binding.layoutEmpty.visible()
                binding.rvOwnerReviews.gone()
            } else {
                binding.layoutEmpty.gone()
                binding.rvOwnerReviews.visible()
            }
        }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            msg ?: return@observe
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }

        viewModel.loadReviews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
