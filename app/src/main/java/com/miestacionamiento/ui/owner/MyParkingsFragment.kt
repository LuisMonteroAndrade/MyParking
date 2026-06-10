package com.miestacionamiento.ui.owner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.miestacionamiento.R
import com.miestacionamiento.data.model.OwnerParking
import com.miestacionamiento.databinding.FragmentMyParkingsBinding

class MyParkingsFragment : Fragment() {

    private var _binding: FragmentMyParkingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyParkingsViewModel by viewModels()
    private lateinit var adapter: OwnerParkingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyParkingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        binding.fabAddParking.setOnClickListener {
            val action = MyParkingsFragmentDirections.actionMyParkingsToParkingForm()
            findNavController().navigate(action)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadParkings()
    }

    private fun setupRecyclerView() {
        adapter = OwnerParkingAdapter(
            onEdit = { parking -> navigateToForm(parking) },
            onDelete = { parking -> showDeleteConfirmDialog(parking) },
            onToggleStatus = { parking -> viewModel.toggleParkingStatus(parking.id) },
            onViewReviews = { findNavController().navigate(R.id.action_myParkings_to_reviews) }
        )
        binding.recyclerParkings.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerParkings.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.parkings.observe(viewLifecycleOwner) { parkings ->
            adapter.submitList(parkings)
            val isEmpty = parkings.isEmpty()
            binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.recyclerParkings.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearMessages()
            }
        }

        viewModel.operationResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                val msg = when (it) {
                    is MyParkingsViewModel.OperationResult.Deleted ->
                        getString(R.string.parking_deleted_success)
                    is MyParkingsViewModel.OperationResult.StatusChanged ->
                        if (it.isActive) getString(R.string.parking_activated_success)
                        else getString(R.string.parking_frozen_success)
                }
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }
    }

    private fun navigateToForm(parking: OwnerParking) {
        val action = MyParkingsFragmentDirections.actionMyParkingsToParkingForm(
            parkingId = parking.id
        )
        findNavController().navigate(action)
    }

    private fun showDeleteConfirmDialog(parking: OwnerParking) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_confirm_title)
            .setMessage(getString(R.string.delete_confirm_message))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.confirm_delete) { _, _ ->
                viewModel.deleteParking(parking.id)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
