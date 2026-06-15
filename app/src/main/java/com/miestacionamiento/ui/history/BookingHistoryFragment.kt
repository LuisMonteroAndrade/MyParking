package com.miestacionamiento.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.miestacionamiento.databinding.FragmentBookingHistoryBinding
import com.miestacionamiento.utils.gone
import com.miestacionamiento.utils.visible
import java.text.NumberFormat
import java.util.Locale

class BookingHistoryFragment : Fragment() {

    private var _binding: FragmentBookingHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookingHistoryViewModel by viewModels()
    private lateinit var adapter: BookingHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        adapter = BookingHistoryAdapter { booking ->
            // Volver a reservar: navegar al detalle del estacionamiento
            val action = BookingHistoryFragmentDirections
                .actionBookingHistoryToDetail(booking.parkingId)
            findNavController().navigate(action)
        }

        binding.rvBookings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBookings.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.load() }

        setupChips()
        setupObservers()
    }

    private fun setupChips() {
        binding.chipAll.setOnClickListener { viewModel.filterAll() }
        binding.chipCompleted.setOnClickListener { viewModel.filterCompleted() }
        binding.chipPending.setOnClickListener { viewModel.filterPending() }
        binding.chipFailed.setOnClickListener { viewModel.filterFailed() }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                binding.progressBar.visible()
                binding.rvBookings.gone()
                binding.layoutEmpty.gone()
            } else {
                binding.progressBar.gone()
                binding.swipeRefresh.isRefreshing = false
            }
        }

        viewModel.bookings.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            if (list.isEmpty()) {
                binding.layoutEmpty.visible()
                binding.rvBookings.gone()
            } else {
                binding.layoutEmpty.gone()
                binding.rvBookings.visible()
            }
        }

        viewModel.totalBookings.observe(viewLifecycleOwner) { count ->
            binding.tvTotalBookings.text = count.toString()
        }

        viewModel.totalSpent.observe(viewLifecycleOwner) { amount ->
            val fmt = NumberFormat.getNumberInstance(Locale("es", "CL"))
            binding.tvTotalSpent.text = "$${fmt.format(amount.toLong())}"
        }

        viewModel.totalHours.observe(viewLifecycleOwner) { hours ->
            binding.tvTotalHours.text = "${hours} h"
        }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            msg ?: return@observe
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
