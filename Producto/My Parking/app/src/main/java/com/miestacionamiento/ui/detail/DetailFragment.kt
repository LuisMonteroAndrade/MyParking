package com.miestacionamiento.ui.detail

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.miestacionamiento.R
import com.miestacionamiento.data.local.entity.ParkingEntity
import com.miestacionamiento.databinding.DialogAddReviewBinding
import com.miestacionamiento.databinding.FragmentDetailBinding
import com.miestacionamiento.databinding.LayoutBookingSheetBinding
import com.miestacionamiento.ui.reviews.ReviewAdapter
import com.miestacionamiento.utils.gone
import com.miestacionamiento.utils.loadUrl
import com.miestacionamiento.utils.toCurrencyString
import com.miestacionamiento.utils.toStarString
import com.miestacionamiento.utils.visible
import java.text.NumberFormat
import java.util.Locale

class DetailFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailViewModel by viewModels()
    private val args: DetailFragmentArgs by navArgs()
    private var googleMap: GoogleMap? = null
    private var currentParking: ParkingEntity? = null

    private var selectedHours = 1
    private var bookingSheet: BottomSheetDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        setupReviews()
        viewModel.loadParking(args.parkingId)

        viewModel.parking.observe(viewLifecycleOwner) { parking ->
            parking?.let {
                currentParking = it
                bindParking(it)
            }
        }

        viewModel.flowPaymentUrl.observe(viewLifecycleOwner) { url ->
            url ?: return@observe
            bookingSheet?.dismiss()
            openFlowPayment(url)
            viewModel.clearFlowPaymentUrl()
        }

        viewModel.bookingResult.observe(viewLifecycleOwner) { result ->
            result ?: return@observe
            bookingSheet?.dismiss()
            when (result) {
                is DetailViewModel.BookingResult.Success -> {
                    Snackbar.make(binding.root, "¡Reserva confirmada! ID: #${result.booking.id}", Snackbar.LENGTH_LONG).show()
                }
                is DetailViewModel.BookingResult.Error -> {
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
            viewModel.clearBookingResult()
        }

        viewModel.reviewResult.observe(viewLifecycleOwner) { msg ->
            msg ?: return@observe
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
            viewModel.clearReviewResult()
        }

        viewModel.chatConversationId.observe(viewLifecycleOwner) { convId ->
            convId ?: return@observe
            val action = DetailFragmentDirections.actionDetailToChat(convId, currentParking?.name ?: "Chat")
            findNavController().navigate(action)
            viewModel.clearChatConversationId()
        }

        viewModel.userType.observe(viewLifecycleOwner) { type ->
            if (type == "DRIVER") {
                binding.btnWriteReview.visible()
                binding.btnChat.visible()
                binding.spacerButtons.visible()
            } else {
                binding.btnWriteReview.gone()
                binding.btnChat.gone()
                binding.spacerButtons.gone()
            }
        }

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.btnSave.setOnClickListener { viewModel.toggleSaved() }
        binding.btnSchedule.setOnClickListener { showBookingSheet() }
        binding.btnWriteReview.setOnClickListener { showReviewDialog() }
        binding.btnChat.setOnClickListener { viewModel.startChat(args.parkingId) }
    }

    private fun setupReviews() {
        val adapter = ReviewAdapter(isOwner = false)
        binding.rvReviews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReviews.adapter = adapter

        viewModel.reviews.observe(viewLifecycleOwner) { reviews ->
            adapter.submitList(reviews)
            if (reviews.isEmpty()) {
                binding.tvNoReviews.visible()
            } else {
                binding.tvNoReviews.gone()
            }
        }
    }

    private fun showReviewDialog() {
        val dialogBinding = DialogAddReviewBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancelReview.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSubmitReview.setOnClickListener {
            val rating = dialogBinding.ratingBarInput.rating.toInt()
            if (rating == 0) {
                Snackbar.make(binding.root, "Selecciona una calificación", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val comment = dialogBinding.etReviewComment.text?.toString()?.trim()
            viewModel.submitReview(args.parkingId, rating, comment?.ifEmpty { null })
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showBookingSheet() {
        val parking = currentParking ?: return
        if (parking.availableSpots == 0) {
            Snackbar.make(binding.root, "No hay lugares disponibles", Snackbar.LENGTH_LONG).show()
            return
        }

        val sheetBinding = LayoutBookingSheetBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(sheetBinding.root)
        bookingSheet = dialog

        sheetBinding.tvSheetParkingName.text = parking.name
        sheetBinding.tvSheetPricePerHour.text = "${parking.pricePerHour.toCurrencyString()}/h"

        selectedHours = 1
        updateSheetTotal(sheetBinding, parking.pricePerHour)

        sheetBinding.toggleGroupHours.check(R.id.btn1h)
        sheetBinding.toggleGroupHours.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedHours = when (checkedId) {
                    R.id.btn1h -> 1
                    R.id.btn2h -> 2
                    R.id.btn3h -> 3
                    R.id.btn4h -> 4
                    else -> 1
                }
                updateSheetTotal(sheetBinding, parking.pricePerHour)
            }
        }

        sheetBinding.btnFlowPay.setOnClickListener {
            sheetBinding.btnFlowPay.isEnabled = false
            sheetBinding.btnFlowPay.text = "Procesando..."
            viewModel.createFlowPayment(parking.id, selectedHours)
        }

        sheetBinding.btnCancelSheet.setOnClickListener {
            viewModel.stopPaymentPolling()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateSheetTotal(sheetBinding: LayoutBookingSheetBinding, pricePerHour: Double) {
        val total = pricePerHour * selectedHours
        val fmt = NumberFormat.getNumberInstance(Locale("es", "CL"))
        sheetBinding.tvSheetTotal.text = "$${fmt.format(total.toLong())}"
    }

    private fun openFlowPayment(url: String) {
        try {
            val colorScheme = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(ContextCompat.getColor(requireContext(), R.color.primary))
                .build()
            val customTab = CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(colorScheme)
                .setShowTitle(true)
                .build()
            customTab.launchUrl(requireContext(), Uri.parse(url))
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        Snackbar.make(binding.root, "Completa el pago y vuelve a la app", Snackbar.LENGTH_LONG).show()
    }

    private fun bindParking(parking: ParkingEntity) {
        binding.collapsingToolbar.title = parking.name
        binding.ivParking.loadUrl(parking.imageUrl)
        binding.tvName.text = parking.name
        binding.tvAddress.text = parking.address
        binding.tvDescription.text = parking.description
        binding.tvPrice.text = parking.pricePerHour.toCurrencyString()
        binding.tvRating.text = parking.rating.toStarString()
        binding.tvReviews.text = "(${parking.reviewCount} reseñas)"
        binding.tvSpots.text = "${parking.availableSpots} de ${parking.totalSpots} lugares disponibles"
        binding.progressSpots.max = parking.totalSpots
        binding.progressSpots.progress = parking.availableSpots

        val saveIcon = if (parking.isSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark
        binding.btnSave.setImageResource(saveIcon)

        val spotsColor = when {
            parking.availableSpots == 0 -> R.color.error
            parking.availableSpots < 10 -> R.color.warning
            else -> R.color.success
        }
        binding.tvSpots.setTextColor(requireContext().getColor(spotsColor))

        googleMap?.let { updateMap(it, parking) }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = true
        currentParking?.let { updateMap(map, it) }
    }

    private fun updateMap(map: GoogleMap, parking: ParkingEntity) {
        val location = LatLng(parking.latitude, parking.longitude)
        map.clear()
        map.addMarker(MarkerOptions().position(location).title(parking.name))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    override fun onResume() { super.onResume(); binding.mapView.onResume() }
    override fun onPause() { super.onPause(); binding.mapView.onPause() }
    override fun onStart() { super.onStart(); binding.mapView.onStart() }
    override fun onStop() { super.onStop(); binding.mapView.onStop() }
    override fun onLowMemory() { super.onLowMemory(); binding.mapView.onLowMemory() }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        bookingSheet?.dismiss()
        binding.mapView.onDestroy()
        super.onDestroyView()
        _binding = null
    }
}
