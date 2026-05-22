package com.miestacionamiento.ui.owner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.miestacionamiento.R
import com.miestacionamiento.databinding.FragmentOwnerDashboardBinding
import java.text.NumberFormat
import java.util.Locale

class OwnerDashboardFragment : Fragment() {

    private var _binding: FragmentOwnerDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OwnerDashboardViewModel by viewModels()
    private lateinit var adapter: RecentBookingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwnerDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadDashboard()
    }

    private fun setupRecyclerView() {
        adapter = RecentBookingAdapter()
        binding.recyclerRecentActivity.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRecentActivity.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.cardMyParkings.setOnClickListener {
            findNavController().navigate(R.id.myParkingsFragment)
        }
        binding.cardAddParking.setOnClickListener {
            findNavController().navigate(R.id.parkingFormFragment)
        }
        binding.cardStats.setOnClickListener {
            findNavController().navigate(R.id.ownerStatsFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.ownerName.observe(viewLifecycleOwner) { name ->
            binding.tvOwnerName.text = name
        }

        viewModel.dashboardData.observe(viewLifecycleOwner) { data ->
            data ?: return@observe

            binding.tvActiveParkings.text = data.activeParkings.toString()
            binding.tvMonthBookings.text = data.monthBookings.toString()

            val fmt = NumberFormat.getNumberInstance(Locale("es", "CL"))
            val revenue = data.monthRevenue.toLong()
            binding.tvMonthRevenue.text = if (revenue >= 1000) {
                "$${fmt.format(revenue / 1000)}K"
            } else {
                "$${fmt.format(revenue)}"
            }

            binding.tvTotalParkings.text = "${data.totalParkings} en total"

            val hasActivity = data.recentBookings.isNotEmpty()
            binding.layoutNoActivity.visibility = if (!hasActivity) View.VISIBLE else View.GONE
            binding.recyclerRecentActivity.visibility = if (!hasActivity) View.GONE else View.VISIBLE
            adapter.submitList(data.recentBookings)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
