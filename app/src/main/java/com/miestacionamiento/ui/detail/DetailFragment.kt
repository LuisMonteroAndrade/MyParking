package com.miestacionamiento.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.miestacionamiento.R
import com.miestacionamiento.data.local.entity.ParkingEntity
import com.miestacionamiento.databinding.FragmentDetailBinding
import com.miestacionamiento.utils.loadUrl
import com.miestacionamiento.utils.toCurrencyString
import com.miestacionamiento.utils.toStarString

class DetailFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailViewModel by viewModels()
    private val args: DetailFragmentArgs by navArgs()
    private var googleMap: GoogleMap? = null
    private var currentParking: ParkingEntity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        viewModel.loadParking(args.parkingId)

        viewModel.parking.observe(viewLifecycleOwner) { parking ->
            parking?.let {
                currentParking = it
                bindParking(it)
            }
        }

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.btnSave.setOnClickListener { viewModel.toggleSaved() }
        binding.btnSchedule.setOnClickListener { showScheduleDialog() }
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

    private fun showScheduleDialog() {
        val parking = currentParking ?: return
        if (parking.availableSpots == 0) {
            Snackbar.make(binding.root, "No hay lugares disponibles en este momento", Snackbar.LENGTH_LONG).show()
            return
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Agendar estacionamiento")
            .setMessage("¿Deseas reservar un lugar en ${parking.name} por ${parking.pricePerHour.toCurrencyString()}?")
            .setPositiveButton("Reservar") { _, _ ->
                Snackbar.make(binding.root, "¡Reserva confirmada! Recibirás un email con los detalles.", Snackbar.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
        binding.mapView.onDestroy()
        super.onDestroyView()
        _binding = null
    }
}
