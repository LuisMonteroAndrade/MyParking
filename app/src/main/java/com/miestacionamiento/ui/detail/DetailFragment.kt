package com.miestacionamiento.ui.detail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.miestacionamiento.R
import com.miestacionamiento.data.local.entity.ParkingEntity
import com.miestacionamiento.databinding.FragmentDetailBinding
import com.miestacionamiento.databinding.LayoutBookingSheetBinding
import com.miestacionamiento.utils.GooglePayHelper
import com.miestacionamiento.utils.loadUrl
import com.miestacionamiento.utils.toCurrencyString
import com.miestacionamiento.utils.toStarString
import java.text.NumberFormat
import java.util.Locale

class DetailFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailViewModel by viewModels()
    private val args: DetailFragmentArgs by navArgs()
    private var googleMap: GoogleMap? = null
    private var currentParking: ParkingEntity? = null

    private lateinit var paymentsClient: com.google.android.gms.wallet.PaymentsClient
    private var googlePayAvailable = false
    private var selectedHours = 1
    private var bookingSheet: BottomSheetDialog? = null

    companion object {
        private const val LOAD_PAYMENT_DATA_REQUEST_CODE = 991
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        setupGooglePay()
        viewModel.loadParking(args.parkingId)

        viewModel.parking.observe(viewLifecycleOwner) { parking ->
            parking?.let {
                currentParking = it
                bindParking(it)
            }
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

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.btnSave.setOnClickListener { viewModel.toggleSaved() }
        binding.btnSchedule.setOnClickListener { showBookingSheet() }
    }

    private fun setupGooglePay() {
        val options = Wallet.WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
            .build()
        paymentsClient = Wallet.getPaymentsClient(requireActivity(), options)

        val request = IsReadyToPayRequest.fromJson(GooglePayHelper.isReadyToPayRequest.toString())
        paymentsClient.isReadyToPay(request)
            .addOnCompleteListener { task ->
                googlePayAvailable = try {
                    task.getResult(ApiException::class.java) == true
                } catch (e: ApiException) {
                    false
                }
            }
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

        if (googlePayAvailable) {
            sheetBinding.btnGooglePay.visibility = View.VISIBLE
            sheetBinding.btnGooglePay.setOnClickListener {
                requestGooglePayment(parking.pricePerHour * selectedHours)
            }
        }

        sheetBinding.btnConfirmBooking.setOnClickListener {
            viewModel.createBooking(parking.id, selectedHours, "", isGooglePay = false)
        }

        sheetBinding.btnCancelSheet.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateSheetTotal(sheetBinding: LayoutBookingSheetBinding, pricePerHour: Double) {
        val total = pricePerHour * selectedHours
        val fmt = NumberFormat.getNumberInstance(Locale("es", "CL"))
        sheetBinding.tvSheetTotal.text = "$${fmt.format(total.toLong())}"
    }

    private fun requestGooglePayment(totalAmount: Double) {
        val paymentDataRequestJson = GooglePayHelper.createPaymentDataRequest(totalAmount)
        val request = PaymentDataRequest.fromJson(paymentDataRequestJson)
        AutoResolveHelper.resolveTask(
            paymentsClient.loadPaymentData(request),
            requireActivity(),
            LOAD_PAYMENT_DATA_REQUEST_CODE
        )
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val paymentData = PaymentData.getFromIntent(data!!)
                    val token = paymentData?.toJson() ?: ""
                    val parking = currentParking ?: return
                    viewModel.createBooking(parking.id, selectedHours, token, isGooglePay = true)
                }
                Activity.RESULT_CANCELED -> {
                    Snackbar.make(binding.root, "Pago cancelado", Snackbar.LENGTH_SHORT).show()
                }
                AutoResolveHelper.RESULT_ERROR -> {
                    val status = AutoResolveHelper.getStatusFromIntent(data)
                    Snackbar.make(
                        binding.root,
                        "Error en Google Pay: ${status?.statusMessage}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
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
