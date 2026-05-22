package com.miestacionamiento.ui.owner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.miestacionamiento.R
import com.miestacionamiento.data.model.OwnerParking
import com.miestacionamiento.databinding.FragmentParkingFormBinding

class ParkingFormFragment : Fragment() {

    private var _binding: FragmentParkingFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ParkingFormViewModel by viewModels()
    private val args: ParkingFormFragmentArgs by navArgs()

    private val isEditMode get() = args.parkingId != -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParkingFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isEditMode) {
            binding.tvFormTitle.setText(R.string.form_edit_parking_title)
            binding.btnSubmit.setText(R.string.btn_update)
            viewModel.loadParking(args.parkingId)
        }

        observeViewModel()

        binding.btnSubmit.setOnClickListener { submitForm() }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSubmit.isEnabled = !loading
        }

        viewModel.isSaving.observe(viewLifecycleOwner) { saving ->
            binding.progressBar.visibility = if (saving) View.VISIBLE else View.GONE
            binding.btnSubmit.isEnabled = !saving
        }

        viewModel.parking.observe(viewLifecycleOwner) { parking ->
            parking?.let { prefillForm(it) }
        }

        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            success?.let {
                val msg = if (isEditMode) getString(R.string.parking_updated_success)
                          else getString(R.string.parking_created_success)
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                viewModel.clearMessages()
                findNavController().popBackStack()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearMessages()
            }
        }
    }

    private fun prefillForm(parking: OwnerParking) {
        binding.etName.setText(parking.name)
        binding.etDescription.setText(parking.description)
        binding.etAddress.setText(parking.address)
        binding.etPrice.setText(parking.pricePerHour.toInt().toString())
        binding.etAvailableSpots.setText(parking.availableSpots.toString())
        binding.etTotalSpots.setText(parking.totalSpots.toString())
        binding.etImageUrl.setText(parking.imageUrl)
    }

    private fun submitForm() {
        clearErrors()

        val name = binding.etName.text?.toString()?.trim() ?: ""
        val description = binding.etDescription.text?.toString()?.trim() ?: ""
        val address = binding.etAddress.text?.toString()?.trim() ?: ""
        val priceStr = binding.etPrice.text?.toString()?.trim() ?: ""
        val availableSpotsStr = binding.etAvailableSpots.text?.toString()?.trim() ?: ""
        val totalSpotsStr = binding.etTotalSpots.text?.toString()?.trim() ?: ""
        val imageUrl = binding.etImageUrl.text?.toString()?.trim() ?: ""

        var valid = true

        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.error_required_fields)
            valid = false
        }
        if (address.isEmpty()) {
            binding.tilAddress.error = getString(R.string.error_required_fields)
            valid = false
        }
        val price = priceStr.toDoubleOrNull()
        if (priceStr.isEmpty() || price == null || price <= 0) {
            binding.tilPrice.error = getString(R.string.error_invalid_price)
            valid = false
        }

        if (!valid) return

        val availableSpots = availableSpotsStr.toIntOrNull() ?: 0
        val totalSpots = totalSpotsStr.toIntOrNull() ?: 0

        viewModel.saveParking(
            parkingId = args.parkingId,
            name = name,
            description = description,
            address = address,
            pricePerHour = price!!,
            availableSpots = availableSpots,
            totalSpots = totalSpots,
            imageUrl = imageUrl
        )
    }

    private fun clearErrors() {
        binding.tilName.error = null
        binding.tilAddress.error = null
        binding.tilPrice.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
